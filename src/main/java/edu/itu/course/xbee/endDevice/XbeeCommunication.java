package edu.itu.course.xbee.endDevice;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;
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
//public class XbeeCommunication implements Runnable {
//
//	private final static Logger log = Logger
//			.getLogger(XbeeCommunication.class);
//
//	// using future
//
//	// using threads
//	@Override
//	public void run() {
//
//		String transferData = XbeeEnum.ERROR_RESPONSE.toString();
//		XBee xbee = new XBee();
//		PropertyReading propertyReading = new PropertyReading();
//		try {
//			
//			    xbee.open(propertyReading.getXbeeDevice(),Integer.parseInt(propertyReading.getXbeeBaud()));
//			    
//				while (true) {
//					
//					try {
//						
//						String receivedString = receiveXbeeData(xbee);
//						log.info("received Command is:"+receivedString);
//						// if get the data is reading
//						if (receivedString.equals(XbeeEnum.READING)) {
//							if (null != getTempSensorData()) {
//								transferData = getTempSensorData();
//							}
//						}
//						// if get the data is relay
//						if (receivedString.equals(XbeeEnum.RELAY_ON)) {
//		
//							relayTheDevice(true);
//							transferData=XbeeEnum.RELAY_ON_DONE.toString();
//						}
//						if (receivedString.equals(XbeeEnum.RELAY_OFF)) {
//		
//							relayTheDevice(false);
//							transferData=XbeeEnum.RELAY_OFF_DONE.toString();
//						} else {
//							log.debug("received unexpected packet "
//									+ receivedString);
//						}
//						log.info("sending to server data is :"+transferData);
//						//response to the server
//						sendXbeeData(xbee,transferData);
//						
//						
//						Thread.sleep(100);
//						
//					} catch (Exception e) {
//						log.error(e);
//					}
//					
//				}
//			} catch (XBeeException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//				log.error(e1);
//			   } finally {
//				  if (xbee != null && xbee.isConnected()) {
//					xbee.close();
//				}
//			  }
//	}


//using packet listener
public class XbeeCommunication {

	private final static Logger log = Logger
			.getLogger(XbeeCommunication.class);

	// using future

	/*
	//using addPacketListener
	Properties props = null;
	
	
	XBee xbee = null;
	PropertyReading propertyReading = null;
	
	Set<Sensor> sensors = null;
	
	
	GpioController gpio = null;

	
	GpioPinDigitalOutput pin = null;
	
	private  XbeeCommunication() {
		
		try {
			Properties props = new Properties();
			
			
			XBee xbee = new XBee();
			PropertyReading propertyReading = new PropertyReading();
			
			Set<Sensor> sensors = null;
			
			
			GpioController gpio = GpioFactory.getInstance();

			
			GpioPinDigitalOutput pin = gpio
					.provisionDigitalOutputPin(RaspiPin.GPIO_12,
							propertyReading.getDeviceName(), PinState.LOW);
				
			props.load(XbeeCommunication.class.getResourceAsStream("/log4j.properties"));
			
			PropertyConfigurator.configure(props);	
			
			log.info("xbee opening---------");
				
		    xbee.open(propertyReading.getXbeeDevice(),Integer.parseInt(propertyReading.getXbeeBaud()));
	//		    xbee.open("/dev/ttyUSB0",9600);
		    
		    log.info("xbee opened---------");
		    
		    sensors = Sensors.getSensors();
		    log.info("found "+sensors.size()+"sensors");
		    
		    xbee.addPacketListener(this);
		    // wait forever 
		    synchronized(this) { this.wait(); } 
	 
			   
			
		} catch (XBeeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			log.error("XBeeException"+e1);
		   }
		catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("InterruptedException"+e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("IOException"+e);
		} finally {
			log.info("coming XBeeException= finally========================");
			  if (xbee != null && xbee.isConnected()) {
				xbee.close();
			}
		  }
	}
	public static void main(String[] args) throws Exception {

		new XbeeCommunication();
		
	}
	@Override
	public void processResponse(XBeeResponse response) {
		// TODO Auto-generated method stub
		  if (response.getApiId() == ApiId.RX_16_RESPONSE) {
    		  
				// we received a packet from .java
				RxResponse16 rx = (RxResponse16) response;

				String receivedString = ByteUtils.toString(rx.getData());
				String transferData = XbeeEnum.ERROR_RESPONSE.toString();
				log.debug("Received RX packet, options is" + rx.getOptions()
						+ ", sender address is " + rx.getRemoteAddress()
						+ ", data is " + receivedString);
				// if get the data is reading
				if (receivedString.equals(XbeeEnum.READING.getValue())) {
					log.info("start reading data :-----");
					try {
						if (null != getTempSensorData(sensors)) {
							transferData = getTempSensorData(sensors);
							
							log.info("received data is:"+transferData);
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// if get the data is relay
				else if (receivedString.equals(XbeeEnum.RELAY_ON.getValue())) {

					log.info("start relayon device :-----");
					relayTheDevice(pin,true);
					transferData=XbeeEnum.RELAY_ON_DONE.getValue();
				}
				else if (receivedString.equals(XbeeEnum.RELAY_OFF.getValue())) {

					log.info("start relayoff device :-----");
					relayTheDevice(pin,false);
					transferData=XbeeEnum.RELAY_OFF_DONE.getValue();
				}
				else if (receivedString.equals(XbeeEnum.GET_DEVICE_NAME.getValue())) {
					
					log.info("start get  device name:-----");
					relayTheDevice(pin,false);
					transferData=XbeeEnum.GET_DEVICE_NAME_DONE.getValue();
				}else {
					log.debug("received unexpected packet "
							+ receivedString);
				}
				log.info("sending to server data is :"+transferData);
				//response to the server
				sendXbeeData(xbee,transferData);
		  }
	}
	*/
	// using getResponse
	
	
	public static void main(String[] args) throws Exception {

		Properties props = new Properties();
		props.load(XbeeCommunication.class.getResourceAsStream("/log4j.properties"));
		
		PropertyConfigurator.configure(props);		
		final XbeeCommunication xbeeCommunication = new XbeeCommunication();
		
		String transferData = XbeeEnum.ERROR_RESPONSE.getValue();
		final XBee xbee = new XBee();
		PropertyReading propertyReading = new PropertyReading();
		
		Set<Sensor> sensors;
		
		
		final GpioController gpio = GpioFactory.getInstance();

		
		final GpioPinDigitalOutput pin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_12,
						propertyReading.getDeviceName(), PinState.LOW);
		try {

			    log.info("xbee opening---------");
			
			    xbee.open(propertyReading.getXbeeDevice(),Integer.parseInt(propertyReading.getXbeeBaud()));
//			    xbee.open("/dev/ttyUSB0",9600);
			    
			    log.info("xbee opened---------");
			    
			    sensors = Sensors.getSensors();
			    
			    log.info("found "+sensors.size()+"sensors");
			    
				while (true) {
					
					try {
						log.info("start receive data from here ---------------");
						String receivedString = xbeeCommunication.receiveXbeeData(xbee);
						log.info("received Command is:"+receivedString);
						// if get the data is reading
						if (receivedString.equals(XbeeEnum.READING.getValue())) {
							log.info("start reading data :-----");
							if (null != xbeeCommunication.getTempSensorData(sensors)) {
								transferData = xbeeCommunication.getTempSensorData(sensors);
								
								log.info("received data is:"+transferData);
							}
						}
						// if get the data is relay
						else if (receivedString.equals(XbeeEnum.RELAY_ON.getValue())) {
		
							log.info("start relayon device :-----");
							xbeeCommunication.relayTheDevice(pin,true);
							transferData=XbeeEnum.RELAY_ON_DONE.getValue();
						}
						else if (receivedString.equals(XbeeEnum.RELAY_OFF.getValue())) {
		
							log.info("start relayoff device :-----");
							xbeeCommunication.relayTheDevice(pin,false);
							transferData=XbeeEnum.RELAY_OFF_DONE.getValue();
						}
						else if (receivedString.equals(XbeeEnum.GET_DEVICE_NAME.getValue())) {
									
									log.info("start get device name :-----");
									xbeeCommunication.relayTheDevice(pin,false);
									transferData=XbeeEnum.GET_DEVICE_NAME_DONE.getValue();
								}else {
							log.debug("received unexpected packet "
									+ receivedString);
						}
						log.info("sending to server data is :"+transferData);
						//response to the server
						xbeeCommunication.sendXbeeData(xbee,transferData);
						
						
						Thread.sleep(100);
						
					} catch (Exception e) {
						log.error(e);
					}
					
				}
			} catch (XBeeException e1) {
				// TODO Auto-generated catch block
				System.out.println("coming XBeeException=========================");

				e1.printStackTrace();
				log.error(e1);
			   } finally {
				   System.out.println("coming XBeeException= finally========================");
				  if (xbee != null && xbee.isConnected()) {
					xbee.close();
				}
			  }
	}

	public String receiveXbeeData(XBee xbee) throws XBeeException {

		try {
			//forever waiting here
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
		}catch(XBeeTimeoutException timeout){
			
			log.info("server timeout"+timeout.getMessage());
			throw new XBeeTimeoutException();
		}
		catch (XBeeException e1)
		{
				// TODO Auto-generated catch block
				e1.printStackTrace();
				log.error(e1);
				throw new XBeeException(e1);
		}
		return null;

	}

	public void sendXbeeData(XBee xbee,String data) {
		//should add into the properties file
		PropertyReading propertyReading = new PropertyReading();
		
		int msb= DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[0];
		int lsb= DatatypeConverter.parseHexBinary(propertyReading.getServerXbeeAddress())[1];
		
		XBeeAddress16 address16 = new XBeeAddress16(msb,lsb);
		
		final int[] payload = data.chars().toArray();
//		final int[] payload = data.toCharArray();
		
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
	public String getTempSensorData(Set<Sensor> sensors) throws IOException {
		
		try {
			for (Sensor sensor : sensors) {
				//reading the temperature data
				if (String.format("%s",sensor.getPhysicalQuantity()).equals("Temperature")) {
					//the right one 
					
					log.debug("String.format(\"%3.2f\", sensor.getValue());"+String.format("%3.2f", sensor.getValue()));
					return String.format("%3.2f", sensor.getValue());
				}
				log.debug(String.format("%s(%s):%3.2f%s", sensor.getPhysicalQuantity(), sensor.getID(), sensor.getValue(), sensor.getUnitString()));
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
				//reading the humidity data
				if (String.format("%s",sensor.getPhysicalQuantity()).equals("Humidity")) {
					//the right one 
					log.debug("String.format(\"%3.2f\", sensor.getValue());"+String.format("%3.2f", sensor.getValue()));
					return String.format("%3.2f", sensor.getValue());				}
				log.info(String.format("%s(%s):%3.2f%s", sensor.getPhysicalQuantity(), sensor.getID(), sensor.getValue(), sensor.getUnitString()));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e);
		}
		
		return null;

	}

	public void relayTheDevice(GpioPinDigitalOutput pin,boolean state) {
		
		
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
