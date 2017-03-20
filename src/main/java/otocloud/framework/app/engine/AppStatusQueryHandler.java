/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import java.util.Map;

import otocloud.framework.app.common.AppConfiguration;
import otocloud.framework.common.OtoCloudServiceState;
import otocloud.framework.core.OtoCloudBusMessage;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppStatusQueryHandler extends AppServiceEngineHandlerImpl<JsonObject> {

	//{appid}.{appGroup}.platform.appinst_status.get
	public static String GET_APPINSTSTATUS_BASE = "platform.appinst_status.get";//响应应用状态查询
	
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppStatusQueryHandler(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
		GET_APPINSTSTATUS_BASE = appServiceEngine.getSrvCfg().getString(AppConfiguration.APP_INST_GROUP) 
				+ "." + GET_APPINSTSTATUS_BASE;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		String account = body.getString("account");
		
		Map<String, AppService> appInstances = appServiceEngine.getAppServiceInstances();		

		if(appInstances.containsKey(account)){
			AppService appServiceInst = appInstances.get(account);
			JsonObject instJs = new JsonObject();
			instJs.put("app_id", appServiceInst.getRealServiceName());
			instJs.put("acct_id", appServiceInst.getAppInstContext().getAccount());
			
			instJs.put("previous_status", "");
			OtoCloudServiceState currentStatus = appServiceInst.getCurrentStatus();
			instJs.put("current_status", currentStatus.toString());
			instJs.put("current_status_desc", OtoCloudServiceState.toDisplay(currentStatus));
			msg.reply(instJs);
		}else{
			msg.fail(100, "无此租户的应用实例");
		}

    }

	/**
	 * {@inheritDoc}
	 */
/*	@Override
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
*/

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return GET_APPINSTSTATUS_BASE;
	}




}
