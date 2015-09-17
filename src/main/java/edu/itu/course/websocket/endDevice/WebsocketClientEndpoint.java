package edu.itu.course.websocket.endDevice;
/**
 * @author  作者 E-mail:
 * @date 创建时间：Sep 16, 2015 2:08:33 PM
 * @version 1.0
 * @parameter 
 * @since
 * @return
 */

import java.io.IOException;
import java.net.URI;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@WebSocket
public class WebsocketClientEndpoint {

    static Session userSession = null;
    private static MessageHandler messageHandler;
    static WebSocketClient client = new WebSocketClient();

    public WebsocketClientEndpoint(URI endpointURI) {
        try {
//            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        	
        	client.start();
        	WebSocketAdapter socket = new WebSocketAdapter() {
        		
                @Override
				public void onWebSocketClose(int statusCode, String reason) {
					// TODO Auto-generated method stub
					super.onWebSocketClose(statusCode, reason);
					
					System.out.println(reason);
					try {
						client.stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onWebSocketError(Throwable cause) {
					// TODO Auto-generated method stub
					super.onWebSocketError(cause);
					System.out.println(cause);
					try {
						client.stop();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onWebSocketText(String message) {
					// TODO Auto-generated method stub
					System.out.println("going to handle"+message);
					super.onWebSocketText(message);
					 if (messageHandler != null) {
				            messageHandler.handleMessage(message);
				        }
					 // send message to websocket
				   sendMessage("message handled=============");
				}

				@Override
                public void onWebSocketConnect(Session session) {
//                   session.getRemote().sendStringByFuture("yo man!");
                   System.out.println("set session");
                   userSession = session;
                }
             };
             ClientUpgradeRequest request =  new ClientUpgradeRequest();
             request.setHeader("deviceId", "1");
        	 client.connect(socket, endpointURI, request);//(socket , endpointURI, request.setHeader("deviceId","1"));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
/*
    *//**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     *//*
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    *//**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     *//*
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("closing websocket");
        this.userSession = null;
    }

    *//**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     *//*
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }
*/
    /**
     * register message handler
     *
     * @param message
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }
    /**
     * Send a message.
     *
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
        try {
			this.userSession.getRemote().sendString(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }
}