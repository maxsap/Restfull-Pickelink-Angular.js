package com.gr.ws.domain;

import java.util.Date;

public class MyMessage {

	private String type;
	private Date receivedAt;
	private String callback_id;
	private boolean result = true;
	
	public MyMessage() {
	}

	public MyMessage(String type, Date receivedAt, String callback_id) {
		this.type = type;
		this.receivedAt = receivedAt;
		this.callback_id = callback_id;
	}

	/**
	 * @return the message
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param message the message to set
	 */
	public void setType(String message) {
		this.type = message;
	}

	/**
	 * @return the receivedAt
	 */
	public Date getReceivedAt() {
		return receivedAt;
	}

	/**
	 * @param receivedAt the receivedAt to set
	 */
	public void setReceivedAt(Date receivedAt) {
		this.receivedAt = receivedAt;
	}

	/**
	 * @return the callback_id
	 */
	public String getCallback_id() {
		return callback_id;
	}

	/**
	 * @param callback_id the callback_id to set
	 */
	public void setCallback_id(String callback_id) {
		this.callback_id = callback_id;
	}

	/**
	 * @return the result
	 */
	public boolean isResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	
}
