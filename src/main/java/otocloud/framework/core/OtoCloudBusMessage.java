/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月14日
 * @author lijing@yonyou.com
 */
public interface OtoCloudBusMessage<T> {
	
	Message<T> getEventMessage();
	
	boolean needAsyncReply();

	String address();

	MultiMap headers();

	T body();

	String replyAddress();


    void reply(Object message);


    <E> void reply(Object message, Handler<AsyncResult<Message<E>>> replyHandler);


    void reply(Object message, DeliveryOptions options);


    <E> void reply(Object message, DeliveryOptions options, Handler<AsyncResult<Message<E>>> replyHandler);
    
    void fail(int failureCode, String message);

}
