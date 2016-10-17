/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.json.JsonObject;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月29日
 * @author lijing@yonyou.com
 */
public class OtoCloudEventDescriptor {
	//地址
	private String eventAddress;
	//事件消息格式
	private JsonObject eventMessageSchema;	
	//是否需要回执
	private boolean needReply = false;
	//回复地址
	private String replyAddress;

	//回执消息格式
	private JsonObject replyMessageSchema;	
	
	//支持web前端响应
	private boolean sendToWeb = false;
	
	/**
	 * @return the sendToWeb
	 */
	public boolean isSendToWeb() {
		return sendToWeb;
	}
	/**
	 * @param sendToWeb the sendToWeb to set
	 */
	public void setSendToWeb(boolean sendToWeb) {
		this.sendToWeb = sendToWeb;
	}
	/**
	 * @return the eventAddress
	 */
	public String getEventAddress() {
		return eventAddress;
	}
	/**
	 * @param eventAddress the eventAddress to set
	 */
	public void setEventAddress(String eventAddress) {
		this.eventAddress = eventAddress;
	}
	
	public String getReplyAddress() {
		return replyAddress;
	}
	
	public void setReplyAddress(String replyAddress) {
		this.replyAddress = replyAddress;
	}
	
	/**
	 * @return the eventMessageSchema
	 */
	public JsonObject getEventMessageSchema() {
		return eventMessageSchema;
	}
	/**
	 * @param eventMessageSchema the eventMessageSchema to set
	 */
	public void setEventMessageSchema(JsonObject eventMessageSchema) {
		this.eventMessageSchema = eventMessageSchema;
	}
	/**
	 * @return the needReply
	 */
	public boolean isNeedReply() {
		return needReply;
	}
	/**
	 * @param needReply the needReply to set
	 */
	public void setNeedReply(boolean needReply) {
		this.needReply = needReply;
	}
	/**
	 * @return the replyMessageSchema
	 */
	public JsonObject getReplyMessageSchema() {
		return replyMessageSchema;
	}
	/**
	 * @param replyMessageSchema the replyMessageSchema to set
	 */
	public void setReplyMessageSchema(JsonObject replyMessageSchema) {
		this.replyMessageSchema = replyMessageSchema;
	}

	
	public OtoCloudEventDescriptor(String eventAddress, JsonObject eventMessageSchema, boolean needReply, JsonObject replyMessageSchema) {
		setEventAddress(eventAddress);
		setEventMessageSchema(eventMessageSchema);
		setNeedReply(needReply);
		setReplyMessageSchema(replyMessageSchema);	
		
	}
	
}
