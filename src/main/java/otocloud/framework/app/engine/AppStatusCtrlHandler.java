/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.vertx.core.Future;
import otocloud.framework.core.OtoCloudBusMessage;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppStatusCtrlHandler extends AppServiceEngineHandlerImpl<JsonObject> {

	//{appid}.platform.appinst_status.control
	public static final String APPINSTS_STATUS_CONTROL_BASE = "platform.appinst_status.control";//应用实例的启动/停止指令
	
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppStatusCtrlHandler(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {

		JsonObject body = msg.body();
		String appInst = body.getString("app_inst");
		String status = body.getString("status");
		String account = null;
		if(body.containsKey("account"))
			account = body.getString("account");
		
		if(!appServiceEngine.checkInstanceScope(appInst)){
			body.put("dispatch_direction", "+");		
			Future<Object> disFuture = Future.future();
			dispatchHandleForAppInst(appInst, body, "+", disFuture);	
			disFuture.setHandler(disRet -> {    		
	    		if (disRet.succeeded()) {
	    			msg.reply(disRet.result());
	    		}else{    			
	               	Throwable err = disRet.cause();       		               	
	               	appServiceEngine.getLogger().error(err.getMessage(), err);
	               	
	    			body.put("dispatch_direction", "-");		
	    			Future<Object> disFuture2 = Future.future();
	    			dispatchHandleForAppInst(appInst, body, "-", disFuture2);	
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
			internalHanle(msg, appInst,status,account);
		}
    }

	@Override
	public void handle2(OtoCloudBusMessage<JsonObject> msg) {

		JsonObject body = msg.body();
		String appInst = body.getString("app_inst");
		String status = body.getString("status");
		String account = null;
		if(body.containsKey("account"))
			account = body.getString("account");
		
		if(!appServiceEngine.checkInstanceScope(appInst)){
			String dispatchChainDirection = body.getString("dispatch_direction");

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
			internalHanle(msg, appInst,status,account);
		}

    }

	private void internalHanle(OtoCloudBusMessage<JsonObject> msg, String appInst,String status,String account){
		JsonObject ret = new JsonObject();
		Map<String, AppService> appInstances = appServiceEngine.getAppServiceInstances();
		if(appInstances.containsKey(appInst)){
			AppService appServiceInst = appInstances.get(appInst);
			try{
				Future<Void> future = Future.future();
				if(status.equals("stop")){
					appServiceInst.close(future);
				}else if(status.equals("startup")){
					appServiceInst.run(future);
				}
				future.setHandler(run -> {    		
		    		if (run.succeeded()) {
		    			if(appServiceEngine.isWebServerHost() && !appServiceInst.isWebServerHost()){   
		    				if(status.equals("stop")){	    					
		    					appServiceEngine.getWebServer().removeOutboundEvent(appServiceInst);		    				
		    				}else if(status.equals("startup")){
		    					appServiceEngine.getWebServer().addOutboundEvent(appServiceInst);
		    				} 
		     			}
		    			ret.put("is_succeeded", "true");
		    			ret.put("current_status", appServiceInst.getCurrentStatus().toString());
		    			msg.reply(ret);
		    		}else{    			
		               	Throwable err = run.cause();               	
		               	msg.fail(400, err.getMessage());
		               	appServiceEngine.getLogger().error(err.getMessage(), err);
		    		}
		    	});
			}catch(Exception e){
               	Throwable err = e.getCause();           	
               	msg.fail(400, err.getMessage());
               	appServiceEngine.getLogger().error(err.getMessage(), err);
			}			
		}else{	
			if(status.equals("startup")){
				if(account == null || account.isEmpty()){
					msg.fail(400, "必须传入account参数才能load应用实例.");
					return;
				}
				
				AppInstRunFuture innerRunFuture = new AppInstRunFuture();			
				innerRunFuture.RunFuture = Future.future();
				
				JsonObject loadAppInstCmd = new JsonObject();
				loadAppInstCmd.put("instid", appInst);
				loadAppInstCmd.put("account", account);
				
				AtomicInteger runningCount = new AtomicInteger(0);
				Integer size = 1;
				
				appServiceEngine.runAppInstance(loadAppInstCmd, innerRunFuture, runningCount, size);
				innerRunFuture.RunFuture.setHandler(run -> {    		
		    		if (run.succeeded()) {
		    			if(appServiceEngine.isWebServerHost()){   				
		    				appServiceEngine.getWebServer().addOutboundEvent(innerRunFuture.AppInst);
		     			}
		    			try{
		    			Future<Void> future = Future.future();		    			
		    			innerRunFuture.AppInst.run(future);
						future.setHandler(instRun -> {    		
				    		if (instRun.succeeded()) {
				    			if(appServiceEngine.isWebServerHost() && !innerRunFuture.AppInst.isWebServerHost()){   
				    				if(status.equals("stop")){	    					
				    					appServiceEngine.getWebServer().removeOutboundEvent(innerRunFuture.AppInst);		    				
				    				}else if(status.equals("startup")){
				    					appServiceEngine.getWebServer().addOutboundEvent(innerRunFuture.AppInst);
				    				} 
				     			}
				    			ret.put("is_succeeded", "true");
				    			ret.put("current_status", innerRunFuture.AppInst.getCurrentStatus().toString());
				    			msg.reply(ret);
				    		}else{    			
				               	Throwable err = instRun.cause(); 				               	
				               	appServiceEngine.getLogger().error(err.getMessage(), err);
				               	msg.fail(400, err.getMessage());
				    		}
				    	});
		    			}
		    			catch(Exception ex){
			               	Throwable err = ex.getCause();           	
			               	appServiceEngine.getLogger().error(err.getMessage(), err);
			               	msg.fail(400, err.getMessage());
		    			}		    			
		    		}else{    			
		               	Throwable err = run.cause();               	
		               	appServiceEngine.getLogger().error(err.getMessage(), err);
		               	msg.fail(400, err.getMessage());
		    		}
		    	});

			}else if(status.equals("stop")){
    			ret.put("is_succeeded", "true");
    			ret.put("current_status", "3");
    			msg.reply(ret);
			}else{
    			ret.put("is_succeeded", "false");
    			ret.put("err", "unknown");
    			msg.reply(ret);
			}			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return APPINSTS_STATUS_CONTROL_BASE;
	}

}
