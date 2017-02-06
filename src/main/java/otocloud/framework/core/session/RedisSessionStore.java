/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.session;



import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public class RedisSessionStore implements SessionStore {

	private RedisOptions config;
	private int expireSeconds = 1800; //默认20分钟session失效
	private Vertx vertx;
	
	public RedisSessionStore(Vertx vertx, JsonObject sessionConfig){	
		expireSeconds = sessionConfig.getInteger("expire_seconds");
		config = new RedisOptions(sessionConfig.getJsonObject("redis_options"));	
		this.vertx = vertx;
	}
	
	public int getExpireSeconds() {
		return expireSeconds;
	}
	
	@Override
	public void get(String sessionId, Handler<AsyncResult<Session>> retSession){
		Future<Session> retFuture = Future.future();
		retFuture.setHandler(retSession);
		
		RedisClient redisClient = RedisClient.create(vertx, config);
		redisClient.ttl(sessionId, ttlRet->{
			if(ttlRet.result() <= 0L){				
				RuntimeException expireException = new RuntimeException("用户会话:" + sessionId + "已过期，请重新登录.");
				retFuture.fail(expireException);
				redisClient.close(closeHandler->{							
				});
			}else{
				redisClient.hset(sessionId, "[EXPIRE]", Integer.toString(expireSeconds), setHandler->{
					//设置超时，已存在则刷新超时
					redisClient.expire(sessionId, expireSeconds, handler->{
						RedisSession session = new RedisSession(sessionId, redisClient);
						retFuture.complete(session);
					});		
				});
				
			}
		});

	}

	@Override
	public void create(String sessionId,
			Handler<AsyncResult<Session>> retSession) {
		Future<Session> retFuture = Future.future();
		retFuture.setHandler(retSession);
		
		RedisClient redisClient = RedisClient.create(vertx, config);

		redisClient.hset(sessionId, "[EXPIRE]", Integer.toString(expireSeconds), setHandler->{
			//设置超时，已存在则刷新超时
			redisClient.expire(sessionId, expireSeconds, handler->{
				RedisSession session = new RedisSession(sessionId, redisClient);
				retFuture.complete(session);
			});		
		});

		
	}

}
