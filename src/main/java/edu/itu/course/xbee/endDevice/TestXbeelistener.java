package edu.itu.course.xbee.endDevice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import se.hirt.w1.Sensor;
import se.hirt.w1.Sensors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeException;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.XBeeTimeoutException;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxStatusResponse;
import com.rapplogic.xbee.util.ByteUtils;

import edu.itu.course.PropertyReading;
import edu.itu.course.XbeeEnum;

public class TestXbeelistener {

	private final static Logger log = Logger.getLogger(TestXbeelistener.class);

	public String receiveXbeeData(XBee xbee) throws XBeeException {

		try {
			// forever waiting here
			XBeeResponse response = xbee.getResponse();

			log.debug("received response " + response.toString());

			if (response.getApiId() == ApiId.RX_16_RESPONSE) {
				// we received a packet from ZNetSenderTest.java
				RxResponse16 rx = (RxResponse16) response;

				log.debug("Received RX packet, options is" + rx.getOptions()
						+ ", sender address is " + rx.getRemoteAddress()
						+ ", data is " + ByteUtils.toString(rx.getData()));

				return ByteUtils.toString(rx.getData());
			}
		} catch (XBeeTimeoutException timeout) {

			log.info("server timeout" + timeout.getMessage());
			throw new XBeeTimeoutException();
		} catch (XBeeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.error(e1);
			throw new XBeeException(e1);
		}
		return null;

	}

	public void sendXbeeData(XBee xbee, String data) {
		// should add into the properties file
		PropertyReading propertyReading = new PropertyReading();

		int msb = DatatypeConverter.parseHexBinary(propertyReading
				.getServerXbeeAddress())[0];
		int lsb = DatatypeConverter.parseHexBinary(propertyReading
				.getServerXbeeAddress())[1];

		XBeeAddress16 address16 = new XBeeAddress16(msb, lsb);

		final int[] payload = data.chars().toArray();
		// final int[] payload = data.toCharArray();

		TxRequest16 request = new TxRequest16(address16, payload);

		log.debug("sending tx packet: " + request.toString());

		try {
			TxStatusResponse response = (TxStatusResponse) xbee
					.sendSynchronous(request, 10000);

			request.setFrameId(xbee.getNextFrameId());

			log.debug("received response " + response);

			if (response.isSuccess()) {
				log.info("response is Success" + response.getStatus());
			} else {
				log.error("response is Error" + response.getStatus());
			}
			// xbee.clearResponseQueue();
			// xbee
		} catch (XBeeTimeoutException e) {
			log.warn("request timed out");
		} catch (XBeeException e) {
			e.printStackTrace();
		}

	}

	public String getTempSensorData(Set<Sensor> sensors) throws IOException {

		try {
			for (Sensor sensor : sensors) {
				// reading the temperature data
				if (String.format("%s", sensor.getPhysicalQuantity()).equals(
						"Temperature")) {
					// the right one

					log.debug("String.format(\"%3.2f\", sensor.getValue());"
							+ String.format("%3.2f", sensor.getValue()));
					return String.format("%3.2f", sensor.getValue());
				}
				log.debug(String.format("%s(%s):%3.2f%s",
						sensor.getPhysicalQuantity(), sensor.getID(),
						sensor.getValue(), sensor.getUnitString()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e);
		}
		return null;

	}

	public String getHumiditySensorData(Set<Sensor> sensors) throws IOException {

		try {

			for (Sensor sensor : sensors) {
				// reading the humidity data
				if (String.format("%s", sensor.getPhysicalQuantity()).equals(
						"Humidity")) {
					// the right one
					log.debug("String.format(\"%3.2f\", sensor.getValue());"
							+ String.format("%3.2f", sensor.getValue()));
					return String.format("%3.2f", sensor.getValue());
				}
				log.info(String.format("%s(%s):%3.2f%s",
						sensor.getPhysicalQuantity(), sensor.getID(),
						sensor.getValue(), sensor.getUnitString()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e);
		}

		return null;

	}

	public void relayTheDevice(GpioPinDigitalOutput pin, boolean state) {

		pin.setState(state);

	}

	public static void main(String[] args) throws Exception {

		// using future

		// using addPacketListener
		// Properties props = null;
		Queue<XBeeResponse> queue = new ConcurrentLinkedQueue<XBeeResponse>();
		XBeeResponse response;
		TestXbeelistener testXbeelistener = new TestXbeelistener();
		Properties props = new Properties();
		XBee xbee = new XBee();
		PropertyReading propertyReading = new PropertyReading();

		Set<Sensor> sensors = Sensors.getSensors();

		GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_12,
						propertyReading.getDeviceName(), PinState.LOW);

		props.load(XbeeCommunicationListener.class
				.getResourceAsStream("/log4j.properties"));

		PropertyConfigurator.configure(props);
		log.info("xbee opening---------");

		xbee.open(propertyReading.getXbeeDevice(),
				Integer.parseInt(propertyReading.getXbeeBaud()));
		// xbee.open("/dev/ttyUSB0",9600);

		log.info("xbee opened---------");

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// sensors = Sensors.getSensors();
		log.info("found " + sensors.size() + "sensors");

		xbee.addPacketListener(new PacketListener() {

			@Override
			public void processResponse(XBeeResponse response) {

				log.info("adding a packet here ----------\n" + queue.size());
				queue.add(response);
			}

		});

		while (true) {

			// we got something!
			try {
				if ((response = queue.poll()) != null) {

					log.info("into while queue.poll here---------------\n");
					// TODO Auto-generated method stub
					if (response.getApiId() == ApiId.RX_16_RESPONSE) {

						// we received a packet from .java
						RxResponse16 rx = (RxResponse16) response;

						String receivedString = ByteUtils
								.toString(rx.getData());
						String transferData = XbeeEnum.ERROR_RESPONSE
								.toString();
						log.info("received Command is:" + receivedString);
						if (null == receivedString) {

							log.info("null data coming");
							continue;
						}
						// if get the data is reading
						if (receivedString.equals(XbeeEnum.READING.getValue())) {
							log.info("start reading data :-----");
							if (null != testXbeelistener
									.getTempSensorData(sensors)) {

								transferData = propertyReading.getDeviceId()
										+ ","
										+ propertyReading.getDeviceName()
										+ ","
										+ testXbeelistener
												.getTempSensorData(sensors)
										+ ","
										+ dateFormat.format(new Date())
												.toString();
								log.info("going to send temp data is:"
										+ transferData);
								testXbeelistener.sendXbeeData(xbee,
										transferData);
							}
						} // if get the data is relay
						else if (receivedString.equals(XbeeEnum.RELAY_ON
								.getValue())) {

							log.info("start relayon device :-----");
							testXbeelistener.relayTheDevice(pin, true);
						} else if (receivedString.equals(XbeeEnum.RELAY_OFF
								.getValue())) {

							log.info("start relayoff device :-----");
							testXbeelistener.relayTheDevice(pin, false);
						}
					}
				}
			} catch (ClassCastException e) {
				// not an IO Sample
				log.error(e.getMessage());
			} catch (Exception e) {
				// not an IO Sample
				log.error(e.getMessage());
			}
		}
	}
}