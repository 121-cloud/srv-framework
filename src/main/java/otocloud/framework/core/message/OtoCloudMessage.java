/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.message;

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
public class OtoCloudMessage {
	
	public OtoCloudMessage(){		
	}
	
	public OtoCloudMessage(String topic, MessageActor sender, String message) {
		super();
		this.topic = topic;
		this.sender = sender;
		this.message = message;
	}
	
	public OtoCloudMessage(String title, String topic, MessageActor sender, String message) {
		super();
		this.title = title;
		this.topic = topic;
		this.sender = sender;
		this.message = message;
	}

	public OtoCloudMessage(String id, MessageActor sender, MessageActor receiver, String message,
			Integer msgStatus) {
		super();
		this.id = id;
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.msgStatus = msgStatus;
	}
	
	public OtoCloudMessage(MessageActor sender, MessageActor receiver, String message,
			Integer msgStatus) {
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.msgStatus = msgStatus;
	}
	
	public OtoCloudMessage(MessageActor sender, MessageActor receiver, String message){
		super();
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}	

	
	/**
	 * @return the sender
	 */
	public MessageActor getSender() {
		return sender;
	}
	/**
	 * @param sender the sender to set
	 */
	public void setSender(MessageActor sender) {
		this.sender = sender;
	}
	/**
	 * @return the receiver
	 */
	public MessageActor getReceiver() {
		return receiver;
	}
	/**
	 * @param receiver the receiver to set
	 */
	public void setReceiver(MessageActor receiver) {
		this.receiver = receiver;
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
	 * @return 0:未处理,1:同意,2：拒绝
	 */
	public Integer getMsgStatus() {
		return msgStatus;
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
	
	
	/**
	 * @return the srcId
	 */
	public String getSrcId() {
		return srcId;
	}

	/**
	 * @param srcId the srcId to set
	 */
	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}


	/**
	 * @return the toId
	 */
	public String getToId() {
		return toId;
	}

	/**
	 * @param toId the toId to set
	 */
	public void setToId(String toId) {
		this.toId = toId;
	}

	protected String srcId = "";
	protected String toId = "";
	protected String id;
	protected String title = "";
	protected String topic;
	protected MessageActor sender;
	protected MessageActor receiver;
	protected String message;
	protected Integer msgStatus = 0; //0:未处理,1:同意,2：拒绝
	protected String sendAt = "";
	protected String handleAt = "";
	protected boolean needReply = true;
	
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

	public JsonObject toJsonObject(){
		JsonObject ret = new JsonObject();
		if(id != null && !id.isEmpty()){
			ret.put("_id", id);
		}
		if(srcId != null && !srcId.isEmpty()){
			ret.put("src_id", srcId);
		}
		if(toId != null && !toId.isEmpty()){
			ret.put("to_id", toId);
		}	
		if(topic != null && !topic.isEmpty()){
			ret.put("topic", topic);
		}

		ret.put("title", title);
		ret.put("sender", sender.toJsonObject());
		if(receiver != null)
			ret.put("receiver", receiver.toJsonObject());
		ret.put("message", message);
		ret.put("msg_status", msgStatus);
		ret.put("sendAt", sendAt);
		ret.put("handleAt", handleAt);
		ret.put("need_reply", needReply);
		
		return ret;
	}
	
	public JsonObject toJsonObjectForDB(){
		JsonObject ret = new JsonObject();		
		
		if(topic != null && !topic.isEmpty()){
			ret.put("topic", topic);
		}

		ret.put("title", title);		
		ret.put("sender", sender.toJsonObject());
		if(receiver != null)
			ret.put("receiver", receiver.toJsonObject());
		ret.put("message", message);
		ret.put("msg_status", msgStatus);
		ret.put("sendAt", sendAt);
		ret.put("handleAt", handleAt);
		ret.put("need_reply", needReply);
		
		if(srcId != null && !srcId.isEmpty()){
			ret.put("src_id", srcId);
		}
		if(toId != null && !toId.isEmpty()){
			ret.put("to_id", toId);
		}
		
		return ret;
	}

	public MessageActor createSender(){
		return new MessageActor();
	}
	
	public MessageActor createReceiver(){
		return new MessageActor();
	}
	
	public void fromJsonObject(JsonObject msgObj){
		if(msgObj.containsKey("_id")){	
			this.id = msgObj.getString("_id");
		}
		this.title = msgObj.getString("title");
		if(msgObj.containsKey("topic")){	
			this.topic = msgObj.getString("topic");
		}		
		
		this.sender = createSender();
		this.sender.fromJsonObject(msgObj.getJsonObject("sender"));
		if(msgObj.containsKey("receiver")){
			JsonObject receJsonObject = msgObj.getJsonObject("receiver");
			if(receJsonObject != null){
				this.receiver = createReceiver();
				this.receiver.fromJsonObject(receJsonObject);
			}
		}
		this.message = msgObj.getString("message");
		this.msgStatus = msgObj.getInteger("msg_status");			
		this.sendAt = msgObj.getString("sendAt");		
		this.handleAt = msgObj.getString("handleAt");		
		this.needReply = msgObj.getBoolean("need_reply");

		if(msgObj.containsKey("src_id")){
			this.srcId = msgObj.getString("src_id");
		}
		if(msgObj.containsKey("to_id")){
			this.toId = msgObj.getString("to_id");
		}
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
	/**
	 * @return the handleAt
	 */
	public String getHandleAt() {
		return handleAt;
	}
	/**
	 * @param handleAt the handleAt to set
	 */
	public void setHandleAt(String handleAt) {
		this.handleAt = handleAt;
	}
	/**
	 * @param msgStatus the msgStatus to set
	 */
	public void setMsgStatus(Integer msgStatus) {
		this.msgStatus = msgStatus;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the topic
	 */
	public String getTopic() {
		return topic;
	}

	/**
	 * @param topic the topic to set
	 */
	public void setTopic(String topic) {
		this.topic = topic;
	}

	
	public static OtoCloudMessage buildTopicMessage(String topic,MessageActor sender, String message){
		return new OtoCloudMessage(topic,sender, message);
	}

	
}
