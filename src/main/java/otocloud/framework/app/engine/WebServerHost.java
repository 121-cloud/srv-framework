/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public interface WebServerHost {
	//是否宿主web服务器
	boolean isWebServerHost();
	
	//创建web服务器
	WebServer createWebServer();
	
	
	void runWebServer();
}
