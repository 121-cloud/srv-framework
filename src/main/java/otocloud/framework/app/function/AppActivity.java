/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.json.JsonObject;

import java.util.List;

import otocloud.framework.app.common.AppInstanceContext;
import otocloud.framework.app.engine.AppService;
import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.app.persistence.OtoCloudCDODataSource;
import otocloud.framework.core.OtoCloudComponent;
import otocloud.framework.core.OtoCloudEventDescriptor;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月21日
 * @author lijing@yonyou.com
 */
public interface AppActivity extends OtoCloudComponent, BizThreadRecorder {
	//获取应用上下文
	AppInstanceContext getAppInstContext();	
	
	OtoCloudAppDataSource getAppDatasource();
	OtoCloudCDODataSource getCDODatasource();
	
	AppService getAppService();
	
	//String getActivityName();
	String getBizObjectType();
	
	//获取业务活动描述
	ActivityDescriptor getActivityDescriptor();
	
	//应用派生类实现，获取活动的业务角色
	//List<OrgRoleDescriptor> exposeBizRolesDesc();	
	
	//公布活动对外发布的事件
	List<OtoCloudEventDescriptor> exposeOutboundBizEventsDesc();
	
	//action
	List<ActionHandlerRegistry> getActionHandlers();	
	

	void setCompleted(String bizObjId, JsonObject actor);
}
