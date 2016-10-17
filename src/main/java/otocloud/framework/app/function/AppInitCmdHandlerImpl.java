/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;


import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import otocloud.framework.app.function.ActionHandlerImpl;
import otocloud.framework.core.OtoCloudBusMessage;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppInitCmdHandlerImpl extends ActionHandlerImpl<JsonObject> {
	
	//适配器响应初始化指令{appid}.{account}.adapter.initialize
	public static final String ADA_INIT_GW = "adapter.initialize";

	//监听应用初始化指令{appid}.{account}.platform.app.initialize
	public static final String LISTEN_APP_INIT_BASE = "platform.app.initialize";
	
	public static final String GET_APPINSTSTATUS = "platform.appinst.dep_status.get";	

	/**
	 * Constructor.
	 *
	 * @param verticle
	 * @param eventAddress
	 */
	public AppInitCmdHandlerImpl(AppActivityImpl appActivity) {
		super(appActivity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
	   	JsonObject body = msg.body();

		//通知网关的适配器进行供应商初始化处理
    	appActivity.getEventBus().<JsonObject> send(
    			appActivity.buildEventAddress(ADA_INIT_GW),
				body,
				reply -> {
					if (reply.succeeded()) {
						msg.reply("ok");
					} else {
						Throwable err = reply.cause();
						String errMsg = err.getMessage();
						appActivity.getLogger().error(errMsg,err);
						msg.fail(500, errMsg);
					}
				});
    }

	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return LISTEN_APP_INIT_BASE;
	}	

	public void appInitialized(Handler<AsyncResult<Boolean>> next){
		Future<Boolean> ret = Future.future();
		ret.setHandler(next);
		
		ret.complete(true);


		getAppStatus(reply->{
			if (reply.succeeded()) {
				String appState = reply.result();
				if(Integer.parseInt(appState) >= 3){
					ret.complete(true);
				}else{
					ret.complete(false);
				}
			} else {
				ret.complete(false);
			}			
		});
	}
	
	
	private void getAppStatus(Handler<AsyncResult<String>> next){
		Future<String> ret = Future.future();
		ret.setHandler(next);
		
		ret.complete("3");
		
		
/*		String appInstId = this.appActivity.getAppInstContext().getInstId();		
		
		JsonObject getAppStatusParam = new JsonObject()
			.put("appinst", appInstId);

		appActivity.getEventBus().<JsonArray>send(GET_APPINSTSTATUS, getAppStatusParam, reply->{
			if (reply.succeeded()) {
				JsonObject appStateObj = reply.result().body().getJsonObject(0);
				ret.complete(String.valueOf(appStateObj.getInteger("appstatus")));
			} else {
				Throwable err = reply.cause();
				String errMsg = "获取应用[" + appInstId + "]状态失败." + err.getMessage();
				componentImpl.getLogger().error(errMsg, err);
				ret.fail(errMsg);
			}			
		});	*/
		
	}



/*	*//**
	 * {@inheritDoc}
	 *//*
	@Override
	public ActionDescriptor getActionDesc() {		
		HandlerDescriptor handlerDescriptor = getHanlderDesc();
		return 	new ActionDescriptor(handlerDescriptor, null, null);
	}*/

}
