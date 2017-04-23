/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudEventDescriptor;
import otocloud.framework.core.OtoCloudEventHandler;
//import io.vertx.core.eventbus.MessageConsumer;
import otocloud.framework.core.session.Session;
import otocloud.framework.core.session.SessionStore;




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
		eventBus.<JsonObject>consumer(getRealAddress(), this::internalHandle);
		
/*		Integer disNoInteger = appServiceEngine.getDistributedNodeIndex();
		if(disNoInteger > 0){
			String address = appServiceEngine.getRealServiceName() + "." + getEventAddress() + "." + disNoInteger.toString();		    	
	    	//consumerForDistribution = eventBus.<T>consumer(address, this::internalHandle2);
	    	eventBus.<T>consumer(address, this::internalHandle2);
		}*/
	}
    
	@Override
	public void internalHandle(Message<JsonObject> msg) {
/*		OtoCloudBusMessage<T> otoMsg = new OtoCloudBusMessageImpl<T>(msg, bus);
		if(otoMsg.needAsyncReply()){
			msg.reply("ok");
		}
		handle(otoMsg);	*/	
		
		System.out.println("服务框架 收到请求消息！");
		
		CommandMessage<T> otoMsg = new CommandMessage<T>(msg, bus);	
		
		sessionHandle(otoMsg, next->{
			if(next.succeeded()){
		    	toHandle(otoMsg);
			}else{
				Throwable err = next.cause();
				//componentImpl.getLogger().error(err.getMessage(), err);
				otoMsg.fail(100, err.getMessage());
			}
		});
	}
	
	private void toHandle(CommandMessage<T> otoMsg){
		if(otoMsg.needAsyncReply()){
			otoMsg.reply("ok");
		}
		handle(otoMsg);
	}
	
	private void sessionHandle(CommandMessage<T> msg, Handler<AsyncResult<Void>> next){
		Future<Void> future = Future.future();
		future.setHandler(next);
		
		MultiMap headers = msg.headers();
		if(headers != null && headers.contains("token")){
			String token = headers.get("token");
			SessionStore sessionStore = appServiceEngine.getSessionStore();
			if(sessionStore != null){
				sessionStore.get(token, sessionRet->{
					if(sessionRet.succeeded()){
						Session session = sessionRet.result();
						session.getAll(retHandler->{
							if(retHandler.succeeded()){
								msg.setSession(retHandler.result());
							}
							future.complete();
							session.close(closeHandler->{							
							});
						});	
					}else{
						Throwable err = sessionRet.cause();
						appServiceEngine.getLogger().error(err.getMessage(), err);
						future.fail(err);
					}
				});				
			}else{
				future.complete();
			}
		}else{
			future.complete();
		}
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