/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;
import otocloud.framework.core.OtoCloudBusMessageImpl;
import otocloud.framework.core.OtoCloudEventDescriptor;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudEventHandler;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import otocloud.framework.core.OtoCloudBusMessage;
//import io.vertx.core.eventbus.MessageConsumer;




/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public abstract class AppServiceEngineHandlerImpl<T> implements OtoCloudEventHandler<T> {
	protected AppServiceEngineImpl appServiceEngine;			
/*	protected MessageConsumer<T> consumer;		
	protected MessageConsumer<T> consumerForDistribution;	*/
	
	protected EventBus bus;
    
    public AppServiceEngineHandlerImpl(AppServiceEngineImpl appServiceEngine) {
    	this.appServiceEngine = appServiceEngine;
    }    
    
    @Override
    public void register(EventBus eventBus) {
    	bus = eventBus;
		//consumer = eventBus.<T>consumer(getRealAddress(), this::internalHandle);
		eventBus.<T>consumer(getRealAddress(), this::internalHandle);
		
/*		Integer disNoInteger = appServiceEngine.getDistributedNodeIndex();
		if(disNoInteger > 0){
			String address = appServiceEngine.getRealServiceName() + "." + getEventAddress() + "." + disNoInteger.toString();		    	
	    	//consumerForDistribution = eventBus.<T>consumer(address, this::internalHandle2);
	    	eventBus.<T>consumer(address, this::internalHandle2);
		}*/
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
    	    	if(consumerForDistribution != null){
    	    		consumerForDistribution.unregister(unregRet->{
    	    			if (unregRet.succeeded()) {
    	    				unregFuture.complete();			  		
    	        		}else{
    	        			unregFuture.fail(completionHandler.cause());
    	        		}    	    			
    	    		});    	    		
    	    	}else{
    	    		unregFuture.complete();		
    	    	}
    		}else{
    			unregFuture.fail(completionHandler.cause());
    		}
    	});*/
    	
    	unregFuture.complete();
	}   
	

/*	public void internalHandle2(Message<T> msg) {
		OtoCloudBusMessage<T> otoMsg = new OtoCloudBusMessageImpl<T>(msg, bus);
		if(otoMsg.needAsyncReply()){
			msg.reply("ok");
		}
		handle2(otoMsg);		
	}*/
   
/*	public void handle2(OtoCloudBusMessage<T> msg) {
    }*/
    
/*    protected void dispatchHandleForAppInst(String acctId, T body, String dispatchChainDirection, Future<Object> ret){

		Integer disNoInteger = appServiceEngine.getDistributedNodeIndex();
		if(dispatchChainDirection.equals("+")){
			disNoInteger = disNoInteger + 1;
		}else {
			if(disNoInteger <= 0){
				ret.fail("找不到应用实例:" + acctId);
				return;
			}					
			disNoInteger = disNoInteger - 1;				
		}
		String address = getEventAddress() + "." + disNoInteger.toString();			
		//正向查找
		appServiceEngine.getBus().<T> send(
				address,
				body,
				//ops,
				reply -> {
					if (reply.succeeded()) {
						Object retObj = reply.result().body();
						ret.complete(retObj);
					} else {		
						Throwable err = reply.cause();						
		               	appServiceEngine.getLogger().error(err.getMessage(), err);
		               	ret.fail(err.getMessage());
					}
		});	

    }*/
    
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