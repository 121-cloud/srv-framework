/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.session;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public class RedisSession implements Session {

	private String sessionId;
	private RedisClient redisClient;
	
	public RedisSession(String sessionId, RedisClient redisClient){		
		this.sessionId = sessionId;
		this.redisClient = redisClient;
	}	

	@Override
	public void close(Handler<AsyncResult<Void>> retHandler){
		redisClient.close(retHandler);
	}

	@Override
	public void setItem(String key, String value,
			Handler<AsyncResult<Long>> retHandler) {		
		redisClient.hset(sessionId, key, value, retHandler);		
	}

	@Override
	public void getItem(String key,
			Handler<AsyncResult<String>> retHandler) {
		redisClient.hget(sessionId, key, retHandler);			
	}

	@Override
	public void getAll(Handler<AsyncResult<JsonObject>> retHandler) {
		redisClient.hgetall(sessionId, retHandler);		
	}

	@Override
	public String getSessionId() {		
		return sessionId;
	}

	@Override
	public void setItems(JsonObject keyValues,
			Handler<AsyncResult<String>> retHandler) {
		redisClient.hmset(sessionId, keyValues, retHandler);		
	}

	@Override
	public void removeAll(Handler<AsyncResult<Long>> retHandler) {
		redisClient.del(sessionId, retHandler);		
	}

}
