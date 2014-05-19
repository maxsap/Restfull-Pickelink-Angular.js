package com.gr.ws.services;

import java.util.Date;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.codehaus.jackson.map.ObjectMapper;
import com.gr.project.security.authorization.annotation.UserLoggedIn;
import com.gr.ws.codecs.EchoServerEncoder;
import com.gr.ws.domain.MyMessage;

@ServerEndpoint(value = "/send", encoders = { EchoServerEncoder.class })
public class WSEchoServer {

	@OnOpen
	@UserLoggedIn
	public void open() {
		System.out.println("Opening the websocket");
	}

	@OnMessage
	public void receiveMessage(String msg, Session session) throws Exception {

		System.out.println("Recieved at server: " + msg);
		
		MyMessage myMsg = new ObjectMapper().readValue(msg, MyMessage.class);

		myMsg.setReceivedAt(new Date());
		// construct the object containing the message and timestamp.
//		MyMessage myMsg = new MyMessage(msg, new Date());
		// Send the message and timestamp data to all the clients connected
		// to this server socket.
		for (Session aSession : session.getOpenSessions()) {
			aSession.getBasicRemote().sendObject(myMsg);
		}
	}

	@OnClose
	public void close(Session session) {
		System.out.println("Closing the websocket");
	}

}
