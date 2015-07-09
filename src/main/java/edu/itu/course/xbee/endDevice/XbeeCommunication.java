package edu.itu.course.xbee.endDevice;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import se.hirt.w1.Sensor;
import se.hirt.w1.Sensors;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.rapplogic.xbee.api.ApiId;
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

public class XbeeCommunication implements Runnable {

	private final static Logger log = Logger
			.getLogger(ZNetReceiverExample.class);

	// using future

	// using threads
	@Override
	public void run() {

		String transferData = XbeeEnum.ERROR_RESPONSE.toString();
		XBee xbee = new XBee();
		PropertyReading propertyReading = new PropertyReading();
		try {
			
			    xbee.open(propertyReading.getXbeeDevice(),Integer.parseInt(propertyReading.getXbeeBaud()));
			    
				while (true) {
					
					try {
						
						String receivedString = receiveXbeeData(xbee);
						log.info("received Command is:"+receivedString);
						// if get the data is reading
						if (receivedString.equals(XbeeEnum.READING)) {
							if (null != getTempSensorData()) {
								transferData = getTempSensorData();
							}
						}
						// if get the data is relay
						if (receivedString.equals(XbeeEnum.RELAY_ON)) {
		
							relayTheDevice(true);
							transferData=XbeeEnum.RELAY_ON_DONE.toString();
						}
						if (receivedString.equals(XbeeEnum.RELAY_OFF)) {
		
							relayTheDevice(false);
							transferData=XbeeEnum.RELAY_OFF_DONE.toString();
						} else {
							log.debug("received unexpected packet "
									+ receivedString);
						}
						log.info("sending to server data is :"+transferData);
						//response to the server
						sendXbeeData(xbee,transferData);
						
						
						Thread.sleep(100);
						
					} catch (Exception e) {
						log.error(e);
					}
					
				}
			} catch (XBeeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error(e1);
			   } finally {
				  if (xbee != null && xbee.isConnected()) {
					xbee.close();
				}
			  }
	}

	public String receiveXbeeData(XBee xbee) {

		try {
			
			XBeeResponse response = xbee.getResponse(10000);

			log.debug("received response " + response.toString());

			if (response.getApiId() == ApiId.RX_16_RESPONSE) {
				// we received a packet from ZNetSenderTest.java
				RxResponse16 rx = (RxResponse16) response;

				log.debug("Received RX packet, options is" + rx.getOptions()
						+ ", sender address is " + rx.getRemoteAddress()
						+ ", data is " + ByteUtils.toString(rx.getData()));
				

				return ByteUtils.toString(rx.getData());
			}
		}catch(XBeeTimeoutException timeout){
			
			log.info("server timeout"+timeout.getMessage());
			
		}
		catch (XBeeException e1)
		{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error(e1);
		}finally {
				  if (xbee != null && xbee.isConnected()) {
					xbee.close();
				}
			  }
		return null;

	}

	public void sendXbeeData(XBee xbee,String data) {
		//should add into the properties file
		
		XBeeAddress16 address16 = new XBeeAddress16(0xFF,0xFF);
		
		final int[] payload = data.chars()
			    .map(x -> x - 48) // 48 is the charcode of 0, 49 of 1 etc.
			    .toArray();
		
		TxRequest16 request = new TxRequest16(address16, payload);
		
		log.debug("sending tx packet: " + request.toString());
		
		try {
			TxStatusResponse response = (TxStatusResponse) xbee.sendSynchronous(request,10000);
			
			request.setFrameId(xbee.getNextFrameId());
			
			log.debug("received response " + response);

			if (response.isSuccess()) {
		        log.info("response is Success"+response.getStatus());
		      } else {
		        log.error("response is Error"+response.getStatus());
		      }
		} catch (XBeeTimeoutException e) {
			log.warn("request timed out");
		} catch (XBeeException e) {
			e.printStackTrace();
		}
		
	}
	public String getTempSensorData() {
		
		Set<Sensor> sensors;
		String tempData=null;
		try {
			sensors = Sensors.getSensors();
		
			for (Sensor sensor : sensors) {
				
				if (sensor.getPhysicalQuantity().equals("Temperature")) {
					//the right one 
					tempData = String.format("%3.2f", sensor.getValue());
				}
				log.info(String.format("%s(%s):%3.2f%s", sensor.getPhysicalQuantity(), sensor.getID(), sensor.getValue(), sensor.getUnitString()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tempData;

	}

	public void relayTheDevice(boolean state) {
		final GpioController gpio = GpioFactory.getInstance();

		PropertyReading propertyReading = new PropertyReading();
		
		final GpioPinDigitalOutput pin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_12,
						propertyReading.getDeviceName(), PinState.LOW);
		
		pin.setState(state);

	}

	// using constructor
	/*
	 * private XbeeCommunication() throws Exception {
	 * 
	 * XBee xbee = new XBee(); // create gpio controller final GpioController
	 * gpio = GpioFactory.getInstance();
	 * 
	 * // provision gpio pin #12 as an output pin and turn on final
	 * GpioPinDigitalOutput pin =
	 * gpio.provisionDigitalOutputPin(RaspiPin.GPIO_12, "MyLED", PinState.LOW);
	 * 
	 * try {
	 * 
	 * // replace with the com port of your receiving XBee (typically your end
	 * device) xbee.open("/dev/ttyUSB0", 9600);
	 * 
	 * while (true) { try { // we wait here until a packet is received.
	 * XBeeResponse response = xbee.getResponse();
	 * 
	 * log.info("received response " + response.toString());
	 * 
	 * if (response.getApiId() == ApiId.RX_16_RESPONSE) { // we received a
	 * packet from ZNetSenderTest.java RxResponse16 rx = (RxResponse16)
	 * response;
	 * 
	 * log.info("Received RX packet, options is" + rx.getOptions()+
	 * ", sender address is "+rx.getRemoteAddress() + ", data is "+
	 * ByteUtils.toString(rx.getData())); // optionally we may want to get the
	 * signal strength (RSSI) of the last hop. // keep in mind if you have
	 * routers in your network, this will be the signal of the last hop.
	 * AtCommand at = new AtCommand("DB"); xbee.sendAsynchronous(at);
	 * XBeeResponse atResponse = xbee.getResponse();
	 * 
	 * if (atResponse.getApiId() == ApiId.AT_RESPONSE) { // remember rssi is a
	 * negative db value log.info("RSSI of last response is " +
	 * -((AtCommandResponse)atResponse).getValue()[0]); } else { // we didn't
	 * get an AT response log.info("expected RSSI, but received " +
	 * atResponse.toString()); }
	 * log.info("<--Pi4J--> GPIO Control Example ... started.");
	 * 
	 * 
	 * // ToggleLED(pin,state); pin.toggle(); Thread.sleep(5000);
	 * 
	 * } else { log.debug("received unexpected packet " + response.toString());
	 * } } catch (Exception e) { log.error(e); } } } finally { if (xbee != null
	 * && xbee.isConnected()) { xbee.close(); } } }
	 */

}
