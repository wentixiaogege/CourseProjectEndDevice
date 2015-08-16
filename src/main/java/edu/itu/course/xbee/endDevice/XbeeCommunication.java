package edu.itu.course.xbee.endDevice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

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


public class XbeeCommunication  {

	private final static Logger log = Logger.getLogger(XbeeCommunication.class);

	// using while true version

	private XbeeCommunication() {

	}
	/**
	 * Reopen.
	 *
	 * @return true, if successful
	 */
	public boolean reopen(XBee xbee,PropertyReading propertyReading){
		try {
			if (xbee != null && xbee.isConnected()) {
				log.info("xbee is shutting down now ---------");
				xbee.close();
				
				log.info("xbee is  opening---------");
				xbee.open(propertyReading.getXbeeDevice(), Integer.parseInt(propertyReading.getXbeeBaud()));

				return true;
			}
			log.info("xbee opening---------");
			xbee.open(propertyReading.getXbeeDevice(), Integer.parseInt(propertyReading.getXbeeBaud()));
			xbee.clearResponseQueue();
			return true;
		} catch (NumberFormatException | XBeeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args) throws Exception {

		Properties props = new Properties();
		props.load(XbeeCommunication.class.getResourceAsStream("/log4j.properties"));

		PropertyConfigurator.configure(props);
		final XbeeCommunication xbeeCommunication = new XbeeCommunication();

		String transferData = XbeeEnum.ERROR_RESPONSE.getValue();
		final XBee xbee = new XBee();
		PropertyReading propertyReading = new PropertyReading();

		DHTSensor sensor = Sensors.getDHTSensor();

		final GpioController gpio = GpioFactory.getInstance();

		final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, propertyReading.getDeviceName(), PinState.LOW);
		try {

			xbeeCommunication.reopen(xbee, propertyReading);

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			while (true) {

				try {
					log.info("start receive data from here ---------------");
					String receivedString = xbeeCommunication.receiveXbeeData(xbee);
					log.info("received Command is:" + receivedString);
					if (null == receivedString) {
						
						log.info("null data coming");
						continue;
					}
					// if get the data is reading
					else if (receivedString.equals(XbeeEnum.READING.getValue())) {
						log.info("start reading data :-----"+xbee.getResponseQueueSize());
//						if (null != xbeeCommunication.getTempSensorData(sensors)) {
							
							transferData = propertyReading.getDeviceId()+","
									      +propertyReading.getDeviceName()+","
									      +sensor.getTemperature() + ","
									      +dateFormat.format(new Date()).toString();
							log.info("going to send temp data is:" + transferData);
							xbeeCommunication.sendXbeeData(xbee, transferData);
//						}
					} // if get the data is relay
					else if (receivedString.equals(XbeeEnum.RELAY_ON.getValue())) {

						log.info("start relayon device :-----");
						xbeeCommunication.relayTheDevice(pin, true);
//						transferData = XbeeEnum.RELAY_ON_DONE.getValue();
					} else if (receivedString.equals(XbeeEnum.RELAY_OFF.getValue())) {

						log.info("start relayoff device :-----");
						xbeeCommunication.relayTheDevice(pin, false);
//						transferData = XbeeEnum.RELAY_OFF_DONE.getValue();
					} 
					
				} catch (Exception e) {
					log.error(e);
				}

			}
		} finally {
			System.out.println("coming XBeeException= finally========================");
			if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		}
	}

	public String receiveXbeeData(XBee xbee) throws XBeeException {

		try {
			// forever waiting here
			XBeeResponse response = xbee.getResponse();

			log.info("received response " + response.toString());

			if (response.getApiId() == ApiId.RX_16_RESPONSE) {
				// we received a packet from ZNetSenderTest.java
				RxResponse16 rx = (RxResponse16) response;

				log.debug("Received RX packet, options is" + rx.getOptions() + ", sender address is " + rx.getRemoteAddress() + ", data is " + ByteUtils.toString(rx.getData()));

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

		int msb = DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[0];
		int lsb = DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[1];

		XBeeAddress16 address16 = new XBeeAddress16(msb, lsb);

		final int[] payload = data.chars().toArray();
		// final int[] payload = data.toCharArray();

		TxRequest16 request = new TxRequest16(address16, payload);

		log.debug("sending tx packet: " + request.toString());

		try {
			TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronous(request, 10000);

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
			reopen(xbee, propertyReading);
		} catch (XBeeException e) {
			e.printStackTrace();
			reopen(xbee, propertyReading);
		}

	}
	public void relayTheDevice(GpioPinDigitalOutput pin, boolean state) {
		pin.setState(state);
	}
}
