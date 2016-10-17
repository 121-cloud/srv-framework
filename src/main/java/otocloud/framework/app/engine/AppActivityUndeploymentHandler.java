/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import otocloud.framework.core.CompUndeploymentHandler;
import io.vertx.core.Future;
import otocloud.framework.core.OtoCloudBusMessage;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppActivityUndeploymentHandler extends AppServiceEngineHandlerForPublish<JsonObject> {

	//{appid}.platform.component.deploy
	//public static final String COMPONENT_UNDEPLOYMENT = "platform.component.undeploy";
	
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppActivityUndeploymentHandler(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
		// TODO Auto-generated constructor stub
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		
		JsonObject body = msg.body();
		String compName = body.getString("comp_name");
		
		if(body.containsKey("account")){
			String account = body.getString("account");
			AppService appInst = appServiceEngine.getAppServiceInst(account);
			if(appInst != null){
				Future<Void> unDepFuture = Future.future();
				appInst.undeployComponent(compName, unDepFuture);
				unDepFuture.setHandler(unDepRet -> {    		
		    		if (unDepRet.succeeded()) {
				    	msg.reply("ok");
		    		}else{    			
		               	Throwable err = unDepRet.cause();               	
		               	appServiceEngine.getLogger().error(err.getMessage(), err);
		               	msg.fail(400, err.getMessage());
		    		}
		    	});				

			}else{
				msg.reply("ok");
			}
			
		}else{
			Future<Void> depFuture = Future.future();
			appServiceEngine.undeployComponent(compName, depFuture);			
			depFuture.setHandler(depRet -> {    		
	    		if (depRet.succeeded()) {	    			
	    			msg.reply("ok");
	    		}else{    			
	               	Throwable err = depRet.cause();               	
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
		return CompUndeploymentHandler.COMPONENT_UNDEPLOYMENT;
	}

}
