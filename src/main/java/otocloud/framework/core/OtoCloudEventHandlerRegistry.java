/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface OtoCloudEventHandlerRegistry {
	String getEventAddress();
	String getApiRegAddress();
	String getRealAddress();
	//void setEventAddress(String eventAddress);
    
    void register(EventBus eventBus);

    void unRegister(Future<Void> unregFuture);    
    
    HandlerDescriptor getHanlderDesc();
    
    String getAPIName();

}