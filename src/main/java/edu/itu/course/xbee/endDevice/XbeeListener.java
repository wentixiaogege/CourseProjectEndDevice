package edu.itu.course.xbee.endDevice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import se.hirt.w1.Sensor;
import se.hirt.w1.Sensors;
import se.hirt.w1.impl.DHTSensor;
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

/**
 * @ClassName: XbeeListener
 * @Description: TODO This Class is used in the end
 * @author Jack Li E-mail:wentixiaogege@gmail.com
 * @date Aug 7, 2015 1:47:49 PM
 * 
 */
public class XbeeListener {

	private final static Logger log = Logger.getLogger(XbeeListener.class);

	/**
	 * @Title: receiveXbeeData
	 * @Description: TODO(describe the functions of this method)
	 * @param @param xbee
	 * @param @return
	 * @param @throws XBeeException
	 * @return String
	 * @throws
	 */
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

	/**
	 * @Title: sendXbeeData
	 * @Description: TODO(describe the functions of this method)
	 * @param @param xbee
	 * @param @param data
	 * @return void
	 * @throws
	 */
	public synchronized void sendXbeeData(XBee xbee, DHTSensor sensor) {
		// should add into the properties file
		PropertyReading propertyReading = new PropertyReading();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		int msb = DatatypeConverter.parseHexBinary(propertyReading
				.getServerXbeeAddress())[0];
		int lsb = DatatypeConverter.parseHexBinary(propertyReading
				.getServerXbeeAddress())[1];

		XBeeAddress16 address16 = new XBeeAddress16(msb, lsb);

		// composing the data format here

		String transferData = propertyReading.getDeviceId() + ","
				+ propertyReading.getDeviceName() + ","
				+ "23.23,"//sensor.getTemperature() + "
				+ dateFormat.format(new Date()).toString();
		final int[] payload = transferData.chars().toArray();

		TxRequest16 request = new TxRequest16(address16, payload);

		log.debug("sending tx packet: " + request.toString());

		try {
			TxStatusResponse response = (TxStatusResponse) xbee
					.sendSynchronous(request, 10000);

			request.setFrameId(xbee.getNextFrameId());

			log.debug("received response " + response);

			if (response.isSuccess()) {
				log.info("response is " + response.getStatus());
			} else {
				log.error("response is " + response.getStatus());
			}
			xbee.clearResponseQueue();
			// xbee
		} catch (XBeeTimeoutException e) {
			log.warn("request timed out");
		} catch (XBeeException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @Title: relayTheDevice
	 * @Description: TODO(describe the functions of this method)
	 * @param @param pin
	 * @param @param state
	 * @return void
	 * @throws
	 */
	public void relayTheDevice(GpioPinDigitalOutput pin, boolean state) {

		pin.setState(state);

	}

	/**
	 * @Title: main
	 * @Description: TODO(describe the functions of this method)
	 * @param @param args
	 * @param @throws Exception
	 * @return void
	 * @throws
	 */
	public static void main(String[] args) throws Exception {

		// Queue<XBeeResponse> queue = new
		// ConcurrentLinkedQueue<XBeeResponse>();
		BlockingQueue<XBeeResponse> queue = new ArrayBlockingQueue<XBeeResponse>(
				30);

		XbeeListener testXbeelistener = new XbeeListener();
		Properties props = new Properties();
		XBee xbee = new XBee();
		PropertyReading propertyReading = new PropertyReading();

		DHTSensor sensor = Sensors.getDHTSensor();

		GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_12,
						propertyReading.getDeviceName(), PinState.LOW);

		props.load(XbeeListener.class.getResourceAsStream("/log4j.properties"));

		PropertyConfigurator.configure(props);
		log.info("xbee opening---------");

		xbee.open(propertyReading.getXbeeDevice(),
				Integer.parseInt(propertyReading.getXbeeBaud()));
		// xbee.open("/dev/ttyUSB0",9600);

		log.info("xbee opened---------");

		// sensors = Sensors.getSensors();
		// log.info("found " + sensors.size() + "sensors");

		xbee.addPacketListener(new PacketListener() {

			@Override
			public void processResponse(XBeeResponse response) {

				log.info("adding a packet here ----------\n" + queue.size());
				// queue.add(response);
				try {
					queue.put(response);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});

		XBeeResponse response;
		while (true) {

			// we got something!
			try {
				//
				log.info("into while queue.poll here---------------\n"
						+ queue.size());
				if ((response = queue.take()) != null) {

					log.info("inside while queue.poll here---------------\n"
							+ queue.size());
					// TODO Auto-generated method stub
					if (response.getApiId() == ApiId.RX_16_RESPONSE) {

						// we received a packet from .java
						RxResponse16 rx = (RxResponse16) response;

						String receivedString = ByteUtils
								.toString(rx.getData());

						log.info("received Command is:" + receivedString);
						if (null == receivedString) {

							log.info("null data coming");
							continue;
						}
						// if get the data is reading
						if (receivedString.equals(XbeeEnum.READING.getValue())) {
							log.info("start reading data :-----xbee size is "
									+ xbee.getResponseQueueSize());

							testXbeelistener.sendXbeeData(xbee, sensor);
						} // if get the data is relay
						else if (receivedString.equals(XbeeEnum.RELAY_ON
								.getValue()+propertyReading.getDeviceId())) {

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
		/*
		 * new Thread(new Runnable() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 * 
		 * }).start();
		 */

	}
}
