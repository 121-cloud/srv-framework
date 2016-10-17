/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;


import java.util.Collection;
import java.util.List;
import java.util.Map;

import otocloud.common.ActionURI;
import otocloud.framework.app.function.ActivityDescriptor;
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
	void addOutboundEvent(AppService appInst);
	void removeOutboundEvent(AppService appInst);
	
	//RESTfull路由
	Map<String, RestActionDescriptor> getActionRoutes();
		
	void init(String srvName, Vertx theVertx, JsonObject webCfg, Logger thelogger);	
	void restRoute(Collection<ActivityDescriptor> activityDescList);
	void busRoute(Collection<AppService> appInstances);
	void listen();
	void close();
}
