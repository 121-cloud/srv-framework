/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import otocloud.common.ActionURI;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public interface WebServer {	
	//最终派生类重写此方法提供action对应的URL
	Map<String, ActionURI> actionUrlSetting();
	
	//提供web前端响应的事件
	List<String> getOutboundEventAddresses();
	void addOutboundEvent(OtoCloudService appInst);
	void removeOutboundEvent(OtoCloudService appInst);
	
	//RESTfull路由
	Map<String, RestActionDescriptor> getActionRoutes();
		
	void init(String srvName, Vertx theVertx, JsonObject webCfg, Logger thelogger);	
	void restRoute(Collection<OtoCloudComponent> activityDescList);
	void busRoute(OtoCloudService appInstance);
	void listen();
	void close();
}
