/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface OtoCloudEventHandler<T> extends OtoCloudEventHandlerRegistry, Handler<CommandMessage<T>> {	
	
    void internalHandle(Message<JsonObject> msg);
}