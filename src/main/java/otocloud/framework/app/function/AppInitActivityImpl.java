/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudEventHandlerRegistry;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public abstract class AppInitActivityImpl extends AppActivityImpl implements AppInitActivity {
	
	//通知应用管理服务应用初始化完成
	public static final String APP_INIT_COMPLETED = "platform.app.initialize.completed";

    
    //派生类根据需要重写
    public AppInitCmdHandlerImpl createInitCmdHandler(){
    	return new AppInitCmdHandlerImpl(this);
    }    


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		List<OtoCloudEventHandlerRegistry> retMap = new ArrayList<OtoCloudEventHandlerRegistry>();		
		
		AppInitCmdHandlerImpl appInitCmdHandler = createInitCmdHandler();
		retMap.add(appInitCmdHandler);
		
		return retMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "appinit";
	}
	
	
	//派生类调用，发布应用初始化完成事件：{app_id,account}
	public void notifyAppInitCompleted(String appId, String account, Handler<AsyncResult<Boolean>> nextHandler) {
		
		Future<Boolean> ret = Future.future();
		ret.setHandler(nextHandler);

    	JsonObject body = new JsonObject();
    	body.put("app_id", appId);
    	body.put("account", account);
		
    	this.getEventBus().<JsonObject> send(
				APP_INIT_COMPLETED,
				body,
				reply -> {
					if (reply.succeeded()) {
						ret.complete(true);						
					} else {
						Throwable err = reply.cause();
						String errMsg = err.getMessage();
						this.getLogger().error(errMsg,err);	
						ret.fail(err);
					}
				});
    	
    }    


}