/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月14日
 * @author lijing@yonyou.com
 */
public class OtoCloudBusMessageImpl<T> implements OtoCloudBusMessage<T> {
	
	public static String REPLY_CHANNEL_TYPE_KEY = "reply_channel";	
	public static String REPLY_ADDR_KEY = "reply_address";
	public static String MSG_ID_KEY = "msg_session_id";
	
	private Message<T> eventMessage;
	private String msgSessionId;
	private String replyAddress;
	private EventBus bus;
	private boolean asyncReply = false;
	
	public boolean needAsyncReply() {
		return asyncReply;
	}

	public Message<T> getEventMessage() {
		return eventMessage;
	}
	
	public OtoCloudBusMessageImpl(Message<T> eventMessage, EventBus bus){
		asyncReply = false;
		this.bus = bus;
		this.eventMessage = eventMessage;	
		MultiMap headers = eventMessage.headers();
		if(headers != null){
			try{
				replyAddress = headers.get(REPLY_ADDR_KEY);				
			}catch (Exception e) {
				replyAddress = "";
			}
			if(replyAddress != null && replyAddress != ""){
				asyncReply = true;
				try{
					msgSessionId = headers.get(MSG_ID_KEY);				
				}catch (Exception e) {
					msgSessionId = "";
				}
			}		
		}
	}

	  @Override
	  public void reply(Object message) {
		  if(asyncReply){
			  reply(message, new DeliveryOptions(), null);
		  }else{
			  eventMessage.reply(message);
		  }
	  }

	  @Override
	  public <R> void reply(Object message, Handler<AsyncResult<Message<R>>> replyHandler) {
		  if(asyncReply)
			  reply(message, new DeliveryOptions(), replyHandler);
		  else{
			  eventMessage.reply(message, new DeliveryOptions(), replyHandler);
		  }
	  }

	  @Override
	  public void reply(Object message, DeliveryOptions options) {
		  if(asyncReply)
			  reply(message, options, null);
		  else{
			  eventMessage.reply(message, options, null);
		  }
	  }

	  @Override
	  public <R> void reply(Object message, DeliveryOptions options, Handler<AsyncResult<Message<R>>> replyHandler) {
		  if(asyncReply){
			  if(msgSessionId != null && msgSessionId != ""){
				  options.addHeader(MSG_ID_KEY, msgSessionId);
			  }			  
			  bus.<R> send(replyAddress, message, options, replyHandler);
		  }
		  else{
			  eventMessage.reply(message, options, replyHandler);
		  }
	  }  

	  
  
	  public static void send(EventBus bus, String address, String replyAddress, String msgId, Object message) {
		  send(bus, address, replyAddress, msgId, message, new DeliveryOptions(), null);
	  }


	  public static <R> void send(EventBus bus, String address, String replyAddress, String msgId, Object message, Handler<AsyncResult<Message<R>>> replyHandler) {
		  send(bus, address, replyAddress, msgId, message, new DeliveryOptions(), replyHandler);
	  }


	  public static void send(EventBus bus, String address, String replyAddress, String msgId,  Object message, DeliveryOptions options) {
	      send(bus, address, replyAddress, msgId, message, options, null);
	  }
	  
	  public static <R> void send(EventBus bus, String address, String replyAddress, String msgId, Object message, DeliveryOptions options, Handler<AsyncResult<Message<R>>> replyHandler) {
		  setReplyAddress(options, replyAddress, msgId);
		  bus.<R> send(address, message, options, replyHandler);
	  }
	  
	  private static void setReplyAddress(DeliveryOptions options, String replyAddress, String msgId){
		  options.addHeader(REPLY_ADDR_KEY, replyAddress);
		  options.addHeader(MSG_ID_KEY, msgId);
	  }
	  

	@Override
	public String address() {
		return eventMessage.address();
	}

	@Override
	public MultiMap headers() {		
		return eventMessage.headers();
	}

	@Override
	public T body() {
		return eventMessage.body();
	}

	@Override
	public String replyAddress() {
		if(asyncReply)
			return replyAddress;
		return eventMessage.replyAddress();
	}

	@Override
	public void fail(int failureCode, String message) {
		if(asyncReply){
			bus.send(replyAddress, new JsonObject()
									.put("failureCode", failureCode)
									.put("message", message));
		}else{
			eventMessage.fail(failureCode, message);
		}
	}
	  
}
