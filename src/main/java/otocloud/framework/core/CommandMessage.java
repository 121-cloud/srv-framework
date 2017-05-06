/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月14日
 * @author lijing@yonyou.com
 */
public class CommandMessage<T> extends OtoCloudBusMessageImpl<JsonObject> {
	
	private JsonObject callContext = new JsonObject();
	
	public CommandMessage(Message<JsonObject> eventMessage, EventBus bus) {
		super(eventMessage, bus);
	}
	
	@SuppressWarnings("unchecked")
	public T getContent(){
		Object ret = this.body().getValue("content");
		return (T)ret;
	}
	
	public JsonObject getQueryParams(){
		return this.body().getJsonObject("query_params", null);		
	}	

	public JsonObject getCallContext() {		
		return callContext;
	}

	public JsonArray getCallChain() {
		if(this.body().containsKey("call_chain")){
			return this.body().getJsonArray("call_chain");
		}else{
			JsonArray ret = new JsonArray();
			this.body().put("call_chain", ret);
			return ret;
		}		
	}
	
	private JsonObject buildSendMessage(Object content){
		JsonObject ret = new JsonObject();
		JsonArray callChain = getCallChain();
		ret.put("call_chain", callChain);
		System.out.println("call_chain: " + callChain.toString());
		ret.put("content", content);
		return ret;
	}
	
	  public <R> void send(String address, Object content, Handler<AsyncResult<Message<R>>> replyHandler) {
		  DeliveryOptions options = new DeliveryOptions();		
		  options.setHeaders(this.headers());
		  this.bus.<R> send(address, buildSendMessage(content), options, replyHandler);
	  }
	  
	  public <R> void send(String address, Object content, long timeout, Handler<AsyncResult<Message<R>>> replyHandler) {
		  DeliveryOptions options = new DeliveryOptions();		
		  options.setHeaders(this.headers());
		  options.setSendTimeout(timeout);		  
		  this.bus.<R> send(address, buildSendMessage(content), options, replyHandler);
	  }

}
