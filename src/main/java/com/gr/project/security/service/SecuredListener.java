package com.gr.project.security.service;

import javax.inject.Inject;

import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.websocket.WebSocketEventListener;
import org.picketlink.idm.IdentityManager;

import com.gr.project.security.authentication.credential.Token;
import com.gr.project.security.authentication.credential.TokenCredential;

public class SecuredListener implements WebSocketEventListener{

	@Inject
    private IdentityManager identity;
	
	@Override
	public void onBroadcast(AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onBroadcast");
	}

	@Override
	public void onClose(AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onClose");
	}

	@Override
	public void onDisconnect(
			AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onDisconnect");
	}

	@Override
	public void onPreSuspend(
			AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onPreSuspend");
	}

	@Override
	public void onResume(AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onResume");
	}

	@Override
	public void onSuspend(AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onSuspend");
	}

	@Override
	public void onThrowable(AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onThrowable");
	}

	@Override
	public void onHeartbeat(AtmosphereResourceEvent paramAtmosphereResourceEvent) {
		// TODO Auto-generated method stub
		System.out.println("onHeartbeat");
	}

	@Override
	public void onClose(WebSocketEvent paramWebSocketEvent) {
		// TODO Auto-generated method stub
		System.out.println("onClose");
	}

	@Override
	public void onConnect(WebSocketEvent paramWebSocketEvent) {
		// TODO Auto-generated method stub
		paramWebSocketEvent.webSocket().resource().getRequest().getHeaderNames();
		identity.validateCredentials(new TokenCredential(""));
		System.out.println("onConnect");
	}

	@Override
	public void onControl(WebSocketEvent paramWebSocketEvent) {
		// TODO Auto-generated method stub
		System.out.println("onControl");
	}

	@Override
	public void onDisconnect(WebSocketEvent paramWebSocketEvent) {
		// TODO Auto-generated method stub
		System.out.println("onDisconnect");
	}

	@Override
	public void onHandshake(WebSocketEvent paramWebSocketEvent) {
		// TODO Auto-generated method stub
		System.out.println("onHandshake");
	}

	@Override
	public void onMessage(WebSocketEvent paramWebSocketEvent) {
		// TODO Auto-generated method stub
		System.out.println("onMessage");
	}

}
