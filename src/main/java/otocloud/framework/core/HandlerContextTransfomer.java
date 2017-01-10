/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;



import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class HandlerContextTransfomer {
	
	public static void fromHttpHeaderToMessageHeader(HttpServerRequest request, DeliveryOptions options){		
		String accessToken = "";
		String actor = "";
		
		try{
			accessToken = request.getHeader(HandlerHttpContext.TOKEN_KEY);
		}catch(Exception e){
			accessToken = "";
		}
		try{
			actor = request.getHeader(HandlerHttpContext.ACTOR_KEY);
		}catch(Exception e){
			actor = "";
		}
		

		if(accessToken != null)
			options.addHeader(HandlerHttpContext.TOKEN_KEY, accessToken); 

			
		if(actor != null)
			options.addHeader(HandlerHttpContext.ACTOR_KEY, actor); 
	}
	
	public static HandlerHttpContext fromHttpRequestParamToMessageHeader(HttpServerRequest request){		
		String context = request.getParam(HandlerHttpContext.CONTEXT_KEY);
		
		HandlerHttpContext ret = new HandlerHttpContext();		
		
		// contex中一定要有四个字段，格式：“{actor}|{access_token}”，如果没有值可以空着，如：“|1310233999”
		// TODO 如果context中没有四个参数直接返回，提示：传入参数不正确
		if (context != null
				&& context.split(HandlerHttpContext.SPLIT_KEY, -1) != null
				&& context.split(HandlerHttpContext.SPLIT_KEY, -1).length == 2) {

			String[] contexts = context.split(HandlerHttpContext.SPLIT_KEY, -1);
			try {
				ret.setAccessToken(contexts[1]);
			} catch (Exception e) {
				ret.setAccessToken("");
			}
			try {
				ret.setActor(contexts[2]);
			} catch (Exception e) {
				ret.setActor("");
			}
			
		}
		return ret;
	}
	
	public static void setBusMessageHeader(HandlerHttpContext httpContext, DeliveryOptions options){		
		options.addHeader(HandlerHttpContext.TOKEN_KEY, httpContext.getAccessToken()); 		

		options.addHeader(HandlerHttpContext.ACTOR_KEY, httpContext.getActor()); 		

	}

	
	public static JsonObject fromMessageHeaderToActor(MultiMap headerMap){	
		JsonObject actorJsonObject = new JsonObject();
		
		//token
		if(headerMap.contains(HandlerHttpContext.TOKEN_KEY)){
			actorJsonObject.put(HandlerHttpContext.TOKEN_KEY, headerMap.get(HandlerHttpContext.TOKEN_KEY));			
		};
		
	
		//操作员
		if(headerMap.contains(HandlerHttpContext.ACTOR_KEY)){
			actorJsonObject.put(HandlerHttpContext.ACTOR_KEY, headerMap.get(HandlerHttpContext.ACTOR_KEY));			
		};
			
		return actorJsonObject;
	}
	
}
