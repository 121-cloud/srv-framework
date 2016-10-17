/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;


import otocloud.framework.core.OtoCloudEventHandlerRegistry;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface ActionHandlerRegistry extends OtoCloudEventHandlerRegistry {
    
    ActionDescriptor getActionDesc();
    
    //应用派生类实现
    //String getActionAddress();
    
	//获取当前账户在当前Action中的业务角色
	//List<BizRole> getActionRolesForCurrentAccount();
}