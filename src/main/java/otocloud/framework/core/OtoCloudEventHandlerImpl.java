/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudEventHandlerImpl<T> extends OtoCloudEventHandlerBase<T> {
	protected OtoCloudComponentImpl componentImpl;			
    
    public OtoCloudEventHandlerImpl(OtoCloudComponentImpl componentImpl) {
    	this.componentImpl = componentImpl;
    }    
   
    @Override
    public String getRealAddress(){
    	String eventAddress = getEventAddress();
    	return componentImpl.buildEventAddress(eventAddress);
    }
    
    @Override
    public String getApiRegAddress(){
    	String eventAddress = getEventAddress();
    	return componentImpl.buildApiRegAddress(eventAddress);
    }
    
}