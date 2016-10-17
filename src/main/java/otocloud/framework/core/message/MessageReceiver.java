/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.message;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public interface MessageReceiver {
	void receive(String account, Handler<AsyncResult<List<OtoCloudMessage>>> retHandler);	
	void stateHandle(OtoCloudMessage message,
			Handler<AsyncResult<Boolean>> retHandler);
}
