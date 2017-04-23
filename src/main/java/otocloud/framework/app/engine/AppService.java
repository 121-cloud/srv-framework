/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;

import otocloud.framework.app.common.AppInstanceContext;
import otocloud.framework.app.function.ActivityDescriptor;
import otocloud.framework.app.function.AppActivity;
import otocloud.framework.app.function.AppInitActivityImpl;
import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.app.persistence.OtoCloudCDODataSource;
import otocloud.framework.core.OtoCloudService;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public interface AppService extends OtoCloudService, WebServerHost {
	//初始化应用实例：只能执行一次
	//void init(AppInstanceContext appInstCtx, JsonObject instCfg, Config clusterCfg, Future<Void> initFuture);	
	void init(AppInstanceContext appInstCtx, JsonObject instCfg, Vertx activityContainer, JsonObject clusterCfg, JsonObject vertxOptionsCfg, Future<Void> initFuture);
	
	AppServiceEngine getAppEngine();
	
	AppInstanceContext getAppInstContext();
	
	OtoCloudAppDataSource getAppDatasource();
	
	OtoCloudCDODataSource getCDODatasource();
	
	void setAppServiceEngine(AppServiceEngine appServiceEngine);
	
	//派生类中重写, 创建应用初始化活动
	AppInitActivityImpl createAppInitActivity();
	
	//派生类中重写,从派生类中获取Activity组件清单
	List<AppActivity> createBizActivities();   
	
	//List<AppActivity> getActivities();   
	List<ActivityDescriptor> getActivityDescriptors();

}
