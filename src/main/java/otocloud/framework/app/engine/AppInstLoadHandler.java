/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import java.util.concurrent.atomic.AtomicInteger;

import otocloud.common.util.JsonUtil;
import io.vertx.core.Future;
import otocloud.framework.core.OtoCloudBusMessage;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppInstLoadHandler extends AppServiceEngineHandlerImpl<JsonObject> {

	//{appid}.platform.app_inst.load
	public static final String APPINST_LOAD_BASE = "platform.app_inst.load";
	
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppInstLoadHandler(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
		// TODO Auto-generated constructor stub
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject appSubscriber = msg.body();
		
		String acctId = JsonUtil.getJsonValue(appSubscriber,"org_acct_id");
		
		if(!appServiceEngine.checkInstanceScope(acctId)){
			appSubscriber.put("dispatch_direction", "+");		
			Future<Object> disFuture = Future.future();
			dispatchHandleForAppInst(acctId, appSubscriber, "+", disFuture);	
			disFuture.setHandler(disRet -> {    		
	    		if (disRet.succeeded()) {
	    			msg.reply(disRet.result());
	    		}else{    			
	               	Throwable err = disRet.cause();       		               	
	               	appServiceEngine.getLogger().error(err.getMessage(), err);
	               	
	               	appSubscriber.put("dispatch_direction", "-");		
	    			Future<Object> disFuture2 = Future.future();
	    			dispatchHandleForAppInst(acctId, appSubscriber, "-", disFuture2);	
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
			AtomicInteger runningCount = new AtomicInteger(0);
			Integer size = 1;
			AppInstRunFuture innerRunFuture = new AppInstRunFuture();			
			innerRunFuture.RunFuture = Future.future();
			appServiceEngine.runAppInstance(msg.body(), innerRunFuture, runningCount, size);
			innerRunFuture.RunFuture.setHandler(run -> {    		
	    		if (run.succeeded()) {
	    			if(appServiceEngine.isWebServerHost()){   				
	    				appServiceEngine.getWebServer().addOutboundEvent(innerRunFuture.AppInst);
	     			}
	    			msg.reply("ok");
	    		}else{    			
	               	Throwable err = run.cause();               	
	               	appServiceEngine.getLogger().error(err.getMessage(), err);
	               	msg.fail(400, err.getMessage());
	    		}
	    	});
		}
    }
	
	
	@Override
	public void handle2(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject appSubscriber = msg.body();
		
		String acctId = JsonUtil.getJsonValue(appSubscriber,"org_acct_id");
		
		if(!appServiceEngine.checkInstanceScope(acctId)){
			String dispatchChainDirection = appSubscriber.getString("dispatch_direction");

			Future<Object> disFuture = Future.future();
			dispatchHandleForAppInst(acctId, appSubscriber, dispatchChainDirection, disFuture);	
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
			AtomicInteger runningCount = new AtomicInteger(0);
			Integer size = 1;
			AppInstRunFuture innerRunFuture = new AppInstRunFuture();			
			innerRunFuture.RunFuture = Future.future();
			appServiceEngine.runAppInstance(msg.body(), innerRunFuture, runningCount, size);
			innerRunFuture.RunFuture.setHandler(run -> {    		
	    		if (run.succeeded()) {
	    			if(appServiceEngine.isWebServerHost()){   				
	    				appServiceEngine.getWebServer().addOutboundEvent(innerRunFuture.AppInst);
	     			}
	    			msg.reply("ok");
	    		}else{    			
	               	Throwable err = run.cause();               	
	               	appServiceEngine.getLogger().error(err.getMessage(), err);
	               	msg.fail(400, err.getMessage());
	    		}
	    	});
		}
    }
	



	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return APPINST_LOAD_BASE;
	}

}
