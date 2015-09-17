package edu.itu.course.sse.endDevice;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.function.Consumer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONObject;

import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;

import se.hirt.w1.Sensors;
import se.hirt.w1.impl.DHTSensor;
import edu.itu.course.PropertyReading;

/**
 * @author 作者 E-mail:
 * @date 创建时间：Sep 15, 2015 12:41:47 PM
 * @version 1.0
 * @parameter
 * @since
 * @return
 */
public class RESTfulEndDevice {

	public static void main(String[] args) {
		// start sse
		RESTfulEndDevice restfulEndDevice = new RESTfulEndDevice();
		
		AsyncRequestProcessor asyncRequestProcessor = restfulEndDevice.new AsyncRequestProcessor();
		
//		asyncRequestProcessor.run();

		
//		Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();

		
//		timer.schedule(taskTest,new Date(), ApplicationValue.getLicenKeyValue());
		// get ip address here
		/*WebTarget target = client
				.target("http://localhost:8111/sse");
		EventListener eventListener = new EventListener() {
			
			
			public void onEvent(InboundEvent inboundEvent) {
				// TODO Auto-generated method stub
				
				System.out.println("12345678"+inboundEvent.readData(String.class));
				
			}
		};
		
		EventSource eventSource =  EventSource.target(target).build();
		
		eventSource.register(eventListener);
		
		eventSource.open();*/
		
		Consumer consumer = new Consumer<String>() {

			public void accept(String t) {
				// TODO Auto-generated method stub
				System.out.println("consumer here");
			}
		};
		try {
			consumeEventStream("http://localhost:8888/frequency",consumer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void consumeEventStream(String url, Consumer consumer) throws Exception {
	    Client client = ClientBuilder.newBuilder().register(new SseFeature()).build();
	    WebTarget target = client.target(url);
	    EventSource e =null;
	    
    	EventListener eventListener = new EventListener() {
			
			
			public void onEvent(InboundEvent inboundEvent) {
				// TODO Auto-generated method stub
				
				System.out.println("12345678"+inboundEvent.readData(String.class));
				
			}
		};
		
	    while (true) {
	        Thread.sleep(1000);
	        if (e==null ||! e.isOpen()) {
	        	 // (re)connect
				EventSource eventSource =  EventSource.target(target).build();
				
				eventSource.register(eventListener);
				
				eventSource.open();
	        }

	        final InboundEvent inboundEvent =null;//e.read();
	        if (inboundEvent == null) {  
	            break;
	        }
	        else {
	           String data = inboundEvent.readData(String.class);
	           // do something here - notify observers, parse json etc
	        }

	    }
	    System.out.println("connection closed");
	}
	/**
	 * This class will create the EventSource and when the SSE are received will
	 * print the data from the Inbound events
	 */
	class TimerTaskTest extends java.util.TimerTask {

		@Override
		public boolean cancel() {
			// TODO Auto-generated method stub
			return super.cancel();
		}
		DHTSensor sensor = Sensors.getDHTSensor();
		PropertyReading propertyReading = new PropertyReading();
		
		
		@Override
		public void run() {
			Client client = ClientBuilder.newClient();
			// period post data to server
			System.out.println("timer here ");
			WebTarget webTarget = client
					.target("http://localhost:8888/service/devices/"
							+ propertyReading.getDeviceId() + "/temperature");

//			Timestamp ts_now = new Timestamp(new Date().getTime());
			DeviceData data = new DeviceData(Integer.parseInt(propertyReading.getDeviceId()),23.23f,new Date());
			Response response = webTarget.request().accept(MediaType.APPLICATION_JSON)
					.post(Entity.entity( data, MediaType.APPLICATION_JSON));
			// check response status code
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatus());
			}
			// display response
			String output = response.readEntity(String.class);
			System.out.println("Output from Server .... "+output);
			
			client.close();
		}
	}

	class AsyncRequestProcessor extends Thread {

		PropertyReading propertyReading = new PropertyReading();

		/*GpioController gpio = GpioFactory.getInstance();

		GpioPinDigitalOutput pin = gpio
				.provisionDigitalOutputPin(RaspiPin.GPIO_12,
						propertyReading.getDeviceName(), PinState.LOW);*/
		
		@Override
		public void run() {
			Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();

			//final Timer timer = new Timer();
			//final TimerTaskTest taskTest = new TimerTaskTest();
//			timer.schedule(taskTest,new Date(), ApplicationValue.getLicenKeyValue());
			// get ip address here
			WebTarget target = client
					.target("http://localhost:8111/sse");
			EventListener eventListener = new EventListener() {
				
				
				public void onEvent(InboundEvent inboundEvent) {
					// TODO Auto-generated method stub
					
					System.out.println("12345678"+inboundEvent.readData(String.class));
					
				}
			};
			
			EventSource eventSource =  EventSource.target(target).build();
			
			eventSource.register(eventListener);
			
			eventSource.open();
			
			/*
			EventSource eventSource = new EventSource(target) {
				@Override
				public void onEvent(InboundEvent inboundEvent) {
					
					// get the JSON data and parse it
					JSONObject jsonObject = JSONObject
							.fromObject(inboundEvent.readData(String.class,
									MediaType.APPLICATION_JSON_TYPE));

					System.out.println("jsonObject.toString()"+jsonObject.toString());
					int relayOn = jsonObject.getInt("relayOn");
					int relayOff = jsonObject.getInt("relayOff");
					int frequency = jsonObject.getInt("frequency");

					// check the value is reasonable

					if (relayOn > 0) {
						System.out.println("relayON  here===================================");
//							pin.setState(true);
					} else if (relayOff > 0) {
						System.out.println("relayOff  here===================================");

//							pin.setState(false);
					} else if (1000 < frequency && frequency < 100000) {

						ApplicationValue.setLicenKeyValue(frequency);
						taskTest.cancel();
						
						timer.schedule(taskTest,
								ApplicationValue.getLicenKeyValue());
					}
				}
			};*/
			
		}
	}
}
