/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudEventHandlerBase<T> implements OtoCloudEventHandler<T> {
	
	protected HandlerDescriptor hanlderDesc;
	
	protected Boolean ignoreAuthVerify = false;


	//protected MessageConsumer<T> consumer;	
	
	protected EventBus bus;
    
    @Override
    public void register(EventBus eventBus) {	
    	bus = eventBus;
 		//consumer = eventBus.<T>consumer(getRealAddress(), this::internalHandle);
    	String realAddr = getRealAddress();    	
    	System.out.println("服务框架handler地址注册：" + realAddr);
 		eventBus.<T>consumer(realAddr, this::internalHandle);
	}
    
    @Override
    public String getRealAddress(){
    	return getEventAddress();    	
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
    public String getApiRegAddress(){
    	return getEventAddress();
    }
    
    @Override
    public HandlerDescriptor getHanlderDesc(){  
    	if(hanlderDesc != null)
    		return hanlderDesc;
		//地址
    	OtoCloudEventDescriptor eventDesc = new OtoCloudEventDescriptor(getApiRegAddress(), null, true, null);    	
		hanlderDesc = new HandlerDescriptor(getAPIName(), eventDesc, null, null, null);
    	return hanlderDesc;    	
    }
    

	@Override
	public String getAPIName() {		
		return this.getClass().getName();
	}
	
	@Override
	public void internalHandle(Message<T> msg){
		System.out.println("服务框架 收到请求消息！");
		
		OtoCloudBusMessage<T> otoMsg = new OtoCloudBusMessageImpl<T>(msg, bus);	
		
		if(otoMsg.needAsyncReply()){
			otoMsg.reply("ok");
		}
		handle(otoMsg);
	}
    
}