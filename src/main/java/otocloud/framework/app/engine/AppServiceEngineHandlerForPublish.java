/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;
import otocloud.framework.core.OtoCloudBusMessage;
import otocloud.framework.core.OtoCloudBusMessageImpl;
import otocloud.framework.core.OtoCloudEventDescriptor;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudEventHandler;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public abstract class AppServiceEngineHandlerForPublish<T> implements OtoCloudEventHandler<T> {
	protected AppServiceEngineImpl appServiceEngine;			
	//protected MessageConsumer<T> consumer;	
	protected EventBus bus;
    
    public AppServiceEngineHandlerForPublish(AppServiceEngineImpl appServiceEngine) {
    	this.appServiceEngine = appServiceEngine;
    }    
    
    @Override
    public void register(EventBus eventBus) {   
    	bus = eventBus;
    	eventBus.<T>consumer(getRealAddress(), this::internalHandle);
		//consumer = eventBus.<T>consumer(getRealAddress(), this::internalHandle);
	}
    
	@Override
	public void internalHandle(Message<T> msg) {
		OtoCloudBusMessage<T> otoMsg = new OtoCloudBusMessageImpl<T>(msg, bus);
		if(otoMsg.needAsyncReply()){
			msg.reply("ok");
		}
		handle(otoMsg);		
	}

	@Override
	public String getRealAddress() {
		return appServiceEngine.buildEventAddress(getEventAddress());
	}
	
	@Override
	public String getApiRegAddress() {
		return getRealAddress();
	}

    @Override
    public void unRegister(Future<Void> unregFuture) {    	
/*    	consumer.unregister(completionHandler->{
    		if (completionHandler.succeeded()) {	
    			unregFuture.complete();    		
    		}else{
    			unregFuture.fail(completionHandler.cause());
    		}
    	});*/
    	unregFuture.complete();
	}
    
    @Override
    public HandlerDescriptor getHanlderDesc(){  	
		//地址
    	OtoCloudEventDescriptor eventDesc = new OtoCloudEventDescriptor(getEventAddress(), null, true, null);    	
    	HandlerDescriptor retDescriptor = new HandlerDescriptor(getAPIName(), eventDesc, null, null, null);
    	return retDescriptor;    	
    }
    
	@Override
	public String getAPIName() {		
		return this.getClass().getName();
	}
	
}