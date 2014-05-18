package com.gr.ws.domain;

import java.util.Date;

public class MyMessage {

	public MyMessage() {
	}

	public MyMessage(String message, Date receivedAt) {
		this.message = message;
		this.receivedAt = receivedAt;
	}

	public String message;
	public Date receivedAt;
}
