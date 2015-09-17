package edu.itu.course.websocket.endDevice;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Timer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONObject;
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
		
		try {
		// open websocket
        final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:8888/frequency/"));
        final Timer timer = new Timer();
		TimerTaskTest taskTest = new RESTfulEndDevice().new TimerTaskTest();
		
        final ReschedulableTimer rescheduleTimer = new ReschedulableTimer();
        rescheduleTimer.schedule(10000);
        
        clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
            public void handleMessage(String message) {
            	System.out.println(message);
            	
            	JSONObject jsonObject =  JSONObject.fromObject(message);
            	
            	int relayOn = jsonObject.getInt("relayOn");
				int relayOff = jsonObject.getInt("relayOff");
				int frequency = jsonObject.getInt("frequency");
            	
            	if (relayOn > 0) {
					System.out.println("relayON  here===================================");
//						pin.setState(true);
				} else if (relayOff > 0) {
					System.out.println("relayOff  here===================================");

//						pin.setState(false);
				} else if (1000 <= frequency && frequency <= 100000) {

					System.out.println("changed===========================");
					
					rescheduleTimer.reschedule(frequency);
	            	
				}
            }
        });

        // send message to websocket
        clientEndPoint.sendMessage("{'event':'addChannel','channel':'ok_btccny_ticker'}");


		//timer.schedule(taskTest,new Date(), ApplicationValue.getLicenKeyValue());
        while (clientEndPoint.userSession.isOpen()) {
//			clientEndPoint.onMessage(message);
//        	
		}
        // wait 5 seconds for messages from websocket
//        Thread.sleep(5000);

    } catch (URISyntaxException ex) {
        System.err.println("URISyntaxException exception: " + ex.getMessage());
    }

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
}
