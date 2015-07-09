/**
 * Copyright (c) 2008 Andrew Rapp. All rights reserved.
 *  
 * This file is part of XBee-API.
 *  
 * XBee-API is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * XBee-API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with XBee-API.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.itu.course.xbee.endDevice;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.rapplogic.xbee.api.ApiId;
import com.rapplogic.xbee.api.AtCommand;
import com.rapplogic.xbee.api.AtCommandResponse;
import com.rapplogic.xbee.api.XBee;
import com.rapplogic.xbee.api.XBeeConfiguration;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.zigbee.ZBNodeDiscover.DeviceType;
import com.rapplogic.xbee.util.ByteUtils;

/**
 * This class is the companion to ZNetSenderTest.java, and as such, it receives packets sent by ZNetSenderTest.java
 * See the ZNetSenderTest.java for information on how to configure your XBee for this demo
 * 
 * You can start ZNetSenderTest.java and this class in any order but it's generally best to start this class first.
 * 
 * @author andrew
 *
 */
public class ZNetReceiverExample {

	
	private final static Logger log = Logger.getLogger(ZNetReceiverExample.class);
	
//    SimpleThreads simpleThreads = new SimpleThreads();
	
	public static void main(String[] args) throws Exception {
		// init log4j
		PropertyConfigurator.configure("log4j.properties");
		
		long patience = 1000 * 60 * 60;
		threadMessage("Starting MessageLoop thread");
	    long startTime = System.currentTimeMillis();
		Thread t = new Thread(new XbeeCommunication());
        t.start();
        // loop until MessageLoop
        // thread exits
        while (t.isAlive()) {
        	threadMessage("Still waiting...");
            // Wait maximum of 1 second
            // for MessageLoop thread
            // to finish.
            t.join(1000);
            
            
           /* if (((System.currentTimeMillis() - startTime) > patience)
                  && t.isAlive()) {
            	threadMessage("Tired of waiting!");
                t.interrupt();
                // Shouldn't be long now
                // -- wait indefinitely
                t.join();
            }*/
        }
	}
	
static void threadMessage(String message) {
	        String threadName =
	            Thread.currentThread().getName();
	        System.out.format("%s: %s%n",
	                          threadName,
	                          message);
	    }
}
