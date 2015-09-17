package edu.itu.course.websocket.endDevice;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import se.hirt.w1.Sensors;
import se.hirt.w1.impl.DHTSensor;
import edu.itu.course.PropertyReading;

/**
 * @author  作者 E-mail:
 * @date 创建时间：Sep 16, 2015 4:53:57 PM
 * @version 1.0
 * @parameter 
 * @since
 * @return
 */
class ReschedulableTimer extends Timer {
    private Runnable task;
    private TimerTask timerTask;

    Timer timer =new Timer();
    
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
    public void schedule(long delay) {

      this.timerTask =  new TimerTaskTest();
      timer.schedule(timerTask,new Date(), delay);        
    }

    public void reschedule(long delay) {
        System.out.println("rescheduling after seconds "+delay);
      timerTask.cancel();
      timerTask = new TimerTaskTest();
      timer.schedule(timerTask, new Date(),delay);        
    }

}