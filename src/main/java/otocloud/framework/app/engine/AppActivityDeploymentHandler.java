/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import otocloud.framework.core.CompDeploymentHandler;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.factory.OtoCloudServiceFactory;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class AppActivityDeploymentHandler extends AppServiceEngineHandlerForPublish<JsonObject> {

	//{appid}.platform.component.deploy
	//public static final String COMPONENT_DEPLOYMENT = "platform.component.deploy";
	
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppActivityDeploymentHandler(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
		// TODO Auto-generated constructor stub
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		
		JsonObject body = msg.body();
		String serviceName = body.getString("service_name");
		JsonObject compDeploymentDesc = body.getJsonObject("component_deployment");
		String compName = OtoCloudServiceFactory.getServiceName(compDeploymentDesc.getString("name"));
		String componentType = compDeploymentDesc.getString("type", null);
		JsonObject compConfig = body.getJsonObject("component_config");
		
		if(componentType != null && componentType.equals("app_engine")){
			Future<Void> depFuture = Future.future();
			appServiceEngine.deployEngineComponent(serviceName, compDeploymentDesc, compConfig, depFuture);			
			depFuture.setHandler(depRet -> {    		
	    		if (depRet.succeeded()) {	    			
	    			msg.reply("ok");
	    		}else{    			
	               	Throwable err = depRet.cause();               	
	               	//getLogger().error(err.getMessage(), err);
	               	msg.fail(400, err.getMessage());
	    		}
	    	});
			return;
		}
		
		
		if(body.containsKey("account")){
			String account = body.getString("account");
			AppService appInst = appServiceEngine.getAppServiceInst(account);
			if(appInst != null){
				Future<Void> unDepFuture = Future.future();
				appInst.undeployComponent(compName, unDepFuture);
				unDepFuture.setHandler(unDepRet -> {    		
		    		if (unDepRet.succeeded()) {	    			
						Future<Void> depFuture = Future.future();
						appInst.deployComponent(serviceName, compDeploymentDesc, compConfig, depFuture);
						depFuture.setHandler(depRet -> {    		
				    		if (depRet.succeeded()) {	    			
				    			msg.reply("ok");
				    		}else{    			
				               	Throwable err = depRet.cause();               	
				               	appServiceEngine.getLogger().error(err.getMessage(), err);
				               	msg.fail(400, err.getMessage());
				    		}
				    	});		    			
		    			
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
			appServiceEngine.deployComponent(serviceName, compDeploymentDesc, compConfig, depFuture);			
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
		return CompDeploymentHandler.COMPONENT_DEPLOYMENT;
	}

}
