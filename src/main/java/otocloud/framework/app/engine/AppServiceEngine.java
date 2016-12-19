/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Map;

import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.app.persistence.OtoCloudCDODataSource;
import otocloud.framework.core.OtoCloudServiceForVerticle;


/**
 * TODO: DOCUMENT ME!
 * 
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public interface AppServiceEngine extends OtoCloudServiceForVerticle, WebServerHost {

	int getDistributedNodeIndex();
	boolean checkInstanceScope(String acctId);	
	
	OtoCloudAppDataSource getAppDatasource();
	OtoCloudCDODataSource getCDODatasource();
	
	void saveConfig(String serviceName, Future<Void> depFuture);
	void saveConfig(Future<Void> depFuture);
	
	//获取应用实例列表
	Map<String, AppService> getAppServiceInstances();
	
	AppService getAppServiceInst(String account);	

	//创建应用实例：派生类重写
	AppService newAppInstance();

	void saveServiceInstConfig(String acctId, JsonObject instCfg, Future<Void> saveFuture);
	
	void deployEngineComponent(String serviceName, JsonObject compDeploymentDesc, JsonObject compConfig, 
			Future<Void> depFuture);
}
