/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月14日
 * @author lijing@yonyou.com
 */
public class CommandMessage<T> extends OtoCloudBusMessageImpl<JsonObject> {

	public CommandMessage(Message<JsonObject> eventMessage, EventBus bus) {
		super(eventMessage, bus);
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unchecked")
	public T getContent(){
		Object ret = this.body().getValue("content");
		return (T)ret;
	}
	
	public JsonObject getQueryParams(){
		return this.body().getJsonObject("queryParams", null);		
	}

}
