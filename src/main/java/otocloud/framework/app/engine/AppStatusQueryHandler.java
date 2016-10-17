/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import java.util.Map;

import otocloud.common.util.JsonUtil;
import otocloud.framework.common.OtoCloudServiceState;
import io.vertx.core.Future;
import otocloud.framework.core.OtoCloudBusMessage;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppStatusQueryHandler extends AppServiceEngineHandlerImpl<JsonArray> {

	//{appid}.appinst_status.get
	public static final String GET_APPINSTSTATUS_BASE = "platform.appinst_status.get";//响应应用状态查询
	
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppStatusQueryHandler(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
		// TODO Auto-generated constructor stub
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonArray> msg) {
		JsonArray body = msg.body();
		Map<String, AppService> appInstances = appServiceEngine.getAppServiceInstances();
		if(body.size() > 0){
			JsonObject item = body.getJsonObject(0);		
			String appInst = JsonUtil.getJsonValue(item, "app_inst");	
			if(!appServiceEngine.checkInstanceScope(appInst)){
				item.put("dispatch_direction", "+");
				
				JsonArray sendBody = new JsonArray();
				sendBody.add(item);
				
				Future<Object> disFuture = Future.future();
				dispatchHandleForAppInst(appInst, sendBody, "+", disFuture);	
				disFuture.setHandler(disRet -> {    		
		    		if (disRet.succeeded()) {
		    			msg.reply(disRet.result());
		    		}else{    			
		               	Throwable err = disRet.cause();       		               	
		               	appServiceEngine.getLogger().error(err.getMessage(), err);
		               	
		               	item.put("dispatch_direction", "-");	
						JsonArray sendBody2 = new JsonArray();
						sendBody2.add(item);
		               	
		    			Future<Object> disFuture2 = Future.future();
		    			dispatchHandleForAppInst(appInst, sendBody2, "-", disFuture2);	
		    			disFuture2.setHandler(disRet2 -> {    		
		    	    		if (disRet2.succeeded()) {
		    	    			msg.reply(disRet2.result());
		    	    		}else{    			
		    	               	Throwable err2 = disRet2.cause();       		               	
		    	               	msg.fail(400, err2.getMessage());
		    	               	appServiceEngine.getLogger().error(err2.getMessage(), err2);
		    	    		}
		    	    	});
		    		}
		    	});
			}else{
				JsonArray ret = new JsonArray();
				if(appInstances.containsKey(appInst)){
					AppService appServiceInst = appInstances.get(appInst);
					JsonObject instJs = new JsonObject();
					instJs.put("app_id", appServiceInst.getRealServiceName());
					instJs.put("app_inst", appInst);
					
					instJs.put("previous_status", "");
					OtoCloudServiceState currentStatus = appServiceInst.getCurrentStatus();
					instJs.put("current_status", currentStatus.toString());
					instJs.put("current_status_desc", OtoCloudServiceState.toDisplay(currentStatus));
					ret.add(instJs);				
				}		
				msg.reply(ret);
			}
		
		}else{
			JsonArray retArray = new JsonArray();
			appInstances.forEach((key,appInstance) -> {
				JsonObject instJs = new JsonObject();
				instJs.put("app_id", appInstance.getRealServiceName());
				instJs.put("acct_id", appInstance.getAppInstContext().getAccount());
				
				instJs.put("previous_status", "");				
				OtoCloudServiceState currentStatus = appInstance.getCurrentStatus();
				instJs.put("current_status", currentStatus.toString());
				instJs.put("current_status_desc", OtoCloudServiceState.toDisplay(currentStatus));
				retArray.add(instJs);
			});	
			msg.reply(retArray);
		}
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle2(OtoCloudBusMessage<JsonArray> msg) {
		JsonArray body = msg.body();
		Map<String, AppService> appInstances = appServiceEngine.getAppServiceInstances();

		JsonObject item = body.getJsonObject(0);		
		String appInst = JsonUtil.getJsonValue(item, "app_inst");	
		if(!appServiceEngine.checkInstanceScope(appInst)){
			String dispatchChainDirection = item.getString("dispatch_direction");
			Future<Object> disFuture = Future.future();
			dispatchHandleForAppInst(appInst, body, dispatchChainDirection, disFuture);	
			disFuture.setHandler(disRet -> {    		
	    		if (disRet.succeeded()) {
	    			msg.reply(disRet.result());
	    		}else{    			
	               	Throwable err = disRet.cause();
	    	        msg.fail(400, err.getMessage());
	               	appServiceEngine.getLogger().error(err.getMessage(), err);     	
	    		}
	    	});			
		}else{
			JsonArray ret = new JsonArray();
			if(appInstances.containsKey(appInst)){
				AppService appServiceInst = appInstances.get(appInst);
				JsonObject instJs = new JsonObject();
				instJs.put("app_id", appServiceInst.getRealServiceName());
				instJs.put("app_inst", appInst);
				
				instJs.put("previous_status", "");
				OtoCloudServiceState currentStatus = appServiceInst.getCurrentStatus();
				instJs.put("current_status", currentStatus.toString());
				instJs.put("current_status_desc", OtoCloudServiceState.toDisplay(currentStatus));
				ret.add(instJs);				
			}		
			msg.reply(ret);
		}
		

    }


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return GET_APPINSTSTATUS_BASE;
	}




}
