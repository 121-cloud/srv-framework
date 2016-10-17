/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.ext.web.Router;

import java.util.List;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public interface OtoCloudRestComponent {
	
	List<OtoCloudRestHandler> getRestHandlers();   
	
	//提供web前端响应的事件
	List<String> getOutboundEventAddresses();	
	
	void beforRoute(Router router);
	void restRoute();
	void busRoute();
	void listen();
	void close();
}
