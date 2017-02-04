/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.session;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public interface SessionStore {	
	void get(String sessionId, Handler<AsyncResult<Session>> retSession);
	void create(String sessionId, Handler<AsyncResult<Session>> retSession);
}
