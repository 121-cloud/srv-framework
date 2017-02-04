/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.session;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public interface Session {
	
	String getSessionId();
	
	void close(Handler<AsyncResult<Void>> retHandler);

	/**
	 * 
	 * @param sessionId
	 * @param key
	 * @param value
	 * @param retHandler 新增则返回1，覆盖则返回0
	 */
	void setItem(String key, String value, Handler<AsyncResult<Long>> retHandler);
	
	/**
	 * 
	 * @param keyValues
	 * @param retHandler 成功返回OK
	 */
	void setItems(JsonObject keyValues, Handler<AsyncResult<String>> retHandler);
	void getItem(String key, Handler<AsyncResult<String>> retHandler);
	void getAll(Handler<AsyncResult<JsonObject>> retHandler);
	
	void removeAll(Handler<AsyncResult<Long>> retHandler);
}
