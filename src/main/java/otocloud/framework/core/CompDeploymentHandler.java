/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class CompDeploymentHandler extends OtoCloudEventHandlerBase<JsonObject> {
	
	//{serviceName}.platform.component.deploy
	public static final String COMPONENT_DEPLOYMENT = "platform.component.deploy";
	
	private OtoCloudService service;
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public CompDeploymentHandler(OtoCloudService service) {
		this.service = service;
	}

	@Override
	public void handle(CommandMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		String serviceName = body.getString("service_name");
		JsonObject compDeploymentDesc = body.getJsonObject("component_deployment");
		JsonObject compConfig = body.getJsonObject("component_config");		

		Future<Void> depFuture = Future.future();
		service.deployComponent(serviceName, compDeploymentDesc, compConfig, depFuture);			
		depFuture.setHandler(depRet -> {    		
    		if (depRet.succeeded()) {	    			
    			msg.reply("ok");
    		}else{    			
               	Throwable err = depRet.cause();               	
               	//getLogger().error(err.getMessage(), err);
               	msg.fail(400, err.getMessage());
    		}
    	});

    }

	public String getEventAddress() {
		return service.getRealServiceName() + "." + COMPONENT_DEPLOYMENT;
	}
	
}
