/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
/*{
	id:<消息ID>，
	sender:{account,app,app_inst,from_role,to_role},
	receiver:{account,app,from_role,to_role},
	message:<邀请消息正文>,
	msgStatus：<消息状态>
	sendAt:
	handleAt:
}*/	
public class BizStateChangedMessage {
	
	/**
	 * Constructor.
	 *
	 * @param app
	 * @param account
	 * @param boType
	 * @param boId
	 * @param previousStatus
	 * @param currentStatus
	 */
	public BizStateChangedMessage(String app, String account, String boType,
			String boId, String previousStatus, String currentStatus) {
		super();
		this.id = java.util.UUID.randomUUID().toString();
		this.app = app;
		this.account = account;
		this.boType = boType;
		this.boId = boId;
		this.previousStatus = previousStatus;
		this.currentStatus = currentStatus;
		formatMessageString();
	}

	public BizStateChangedMessage(){		
	}
	
	public void formatMessageString(){
		this.message = String.format("账户[%s]的%s[%s]状态发生变化，变化内容为：%s->%s", 
				account, boId, boType,previousStatus,currentStatus);
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	protected String id;
	protected String app;
	protected String account;
	protected String boType;
	protected String boId;
	protected String previousStatus;
	protected String currentStatus;
	
	protected String message;
	protected String sendAt = "";
	


	public JsonObject toJsonObject(){
		JsonObject ret = new JsonObject();

		ret.put("id", id);
		ret.put("app", app);
		ret.put("account", account);
		ret.put("bo_type", boType);
		ret.put("bo_id", boId);
		ret.put("previous_status", previousStatus);
		ret.put("current_status", currentStatus);
		ret.put("message", message);
		ret.put("sendAt", sendAt);
		
		return ret;
	}

	
	public void fromJsonObject(JsonObject msgObj){

		this.id = msgObj.getString("id");
		this.app = msgObj.getString("app");	
		this.account = msgObj.getString("account");
		this.boType = msgObj.getString("bo_type");		
		this.boId = msgObj.getString("bo_id");
		this.previousStatus = msgObj.getString("previous_status");	
		this.currentStatus = msgObj.getString("current_status");
		this.message = msgObj.getString("message");		
		this.sendAt = msgObj.getString("sendAt");		


	}

	/**
	 * @return the app
	 */
	public String getApp() {
		return app;
	}

	/**
	 * @param app the app to set
	 */
	public void setApp(String app) {
		this.app = app;
	}

	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * @return the boType
	 */
	public String getBoType() {
		return boType;
	}

	/**
	 * @param boType the boType to set
	 */
	public void setBoType(String boType) {
		this.boType = boType;
	}

	/**
	 * @return the boId
	 */
	public String getBoId() {
		return boId;
	}

	/**
	 * @param boId the boId to set
	 */
	public void setBoId(String boId) {
		this.boId = boId;
	}

	/**
	 * @return the previousStatus
	 */
	public String getPreviousStatus() {
		return previousStatus;
	}

	/**
	 * @param previousStatus the previousStatus to set
	 */
	public void setPreviousStatus(String previousStatus) {
		this.previousStatus = previousStatus;
	}

	/**
	 * @return the currentStatus
	 */
	public String getCurrentStatus() {
		return currentStatus;
	}

	/**
	 * @param currentStatus the currentStatus to set
	 */
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	/**
	 * @return the sendAt
	 */
	public String getSendAt() {
		return sendAt;
	}

	/**
	 * @param sendAt the sendAt to set
	 */
	public void setSendAt(String sendAt) {
		this.sendAt = sendAt;
	}
	


	
}
