/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import otocloud.common.OtoCloudLogger;
import otocloud.persistence.dao.JdbcDataSource;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月21日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudComponentImpl extends AbstractVerticle implements OtoCloudComponent {

	protected OtoCloudLogger logger;
	protected String comInstTag = "";	
	protected OtoCloudService service;
	protected JsonObject dependencies;
	
	protected List<OtoCloudEventHandlerRegistry> eventHandlers;
	
	
	@Override 
	public void init(Vertx vertx, Context context){
		super.init(vertx, context);
		
		ContextImpl contextImpl = (ContextImpl)this.context;
		DeploymentOptions deploymentOptions = contextImpl.getDeployment().deploymentOptions();
		
		OtoCloudCompDepOptions otoCloudDeploymentOptions = (OtoCloudCompDepOptions)deploymentOptions;
		service = otoCloudDeploymentOptions.getService();	
		//service.registerComponent(this);	

	}	
	
	@Override 
    public void start(Future<Void> startFuture) throws Exception {
		
		//所依赖的服务
		dependencies = config().getJsonObject("dependencies", null);
		
		configLogging();	
		eventHandlers = registerEventHandlers();
		if(eventHandlers != null && eventHandlers.size() > 0){
			List<HandlerDescriptor> restAPIs = new ArrayList<HandlerDescriptor>();
			eventHandlers.forEach(value -> {
				value.register(getEventBus());
				HandlerDescriptor handlerDesc= value.getHanlderDesc();
				if(handlerDesc.getRestApiURI() != null){
					restAPIs.add(handlerDesc);
				}
			});
			if(restAPIs.size() > 0){
				service.registerRestAPIs(getName(), restAPIs, startFuture);
			}else{
				startFuture.complete();
			}
		}else{
			startFuture.complete();
		}

	}
	
/*	@Override 
    public void stop() {		
		if(eventHandlers != null && eventHandlers.size() > 0){
			eventHandlers.forEach(value -> {
				value.unRegister();
			});
		}	
		if(logger != null){
			logger.info(this.getClass().getName() + " exit!");
		}
	}	*/
	
	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		if(eventHandlers != null && eventHandlers.size() > 0){
			Integer size = eventHandlers.size();
			AtomicInteger stoppedCount = new AtomicInteger(0);			
			eventHandlers.forEach(value -> {
				Future<Void> unregFuture = Future.future();
				value.unRegister(unregFuture);				
				unregFuture.setHandler(unregRet -> {
            		if(unregRet.succeeded()){   

            		}else{
            			Throwable err = unregRet.cause();
            			err.printStackTrace(); 
            			stopFuture.fail(err);
            			return;
            		}
            		
               		if (stoppedCount.incrementAndGet() >= size) {                 			
               			logger.info("Component:[" + this.getName() + "] exist!");
               			stopFuture.complete();
                    }         		
            		
               	});	               	
			});
		}else{
			logger.info("Component:[" + this.getName() + "] exist!");
			stopFuture.complete();
		}

	}
	
	@Override 
	public EventBus getEventBus(){
		return vertx.eventBus();
	}
	
	@Override 
	public OtoCloudService getService() {
		return service;
	}
	
	@Override
	public String buildEventAddress(String addressBase){
		return service.buildEventAddress( getName() + "." + addressBase );
	}
	
	@Override
	public String buildApiRegAddress(String addressBase){
		return buildEventAddress(addressBase);
	}
	
	@Override
	public JdbcDataSource getSysDatasource(){
		return service.getSysDatasource();
	}
	
	@Override
	public OtoCloudLogger getLogger(){
		return logger;
	}
	
	protected void configLogging() {
		logger = new OtoCloudLogger(comInstTag, LoggerFactory.getLogger(this.getClass().getName()));
	}	
	
	@Override
	public void saveConfig(Future<Void> saveFuture){
		this.getService().saveComponentConfig(this.getName(), config(), saveFuture);
	}
	
	@Override
	public JsonObject getDependencies() {
		return dependencies;
	}
	
}
