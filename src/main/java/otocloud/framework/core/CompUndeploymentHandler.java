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
public class CompUndeploymentHandler extends OtoCloudEventHandlerBase<JsonObject> {
	
	//{serviceName}.platform.component.undeploy
	public static final String COMPONENT_UNDEPLOYMENT = "platform.component.undeploy";
	
	private OtoCloudService service;
	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public CompUndeploymentHandler(OtoCloudService service) {
		this.service = service;
	}

	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		String compName = body.getString("comp_name");

		Future<Void> depFuture = Future.future();
		service.undeployComponent(compName, depFuture);			
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
		return service.getRealServiceName() + "." + COMPONENT_UNDEPLOYMENT;
	}
	
}
