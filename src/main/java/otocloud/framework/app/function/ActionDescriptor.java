/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import otocloud.framework.core.HandlerDescriptor;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月29日
 * @author lijing@yonyou.com
 */
public class ActionDescriptor {
	private HandlerDescriptor handlerDescriptor;

	public HandlerDescriptor getHandlerDescriptor() {
		return handlerDescriptor;
	}
	public void setHandlerDescriptor(HandlerDescriptor handlerDescriptor) {
		this.handlerDescriptor = handlerDescriptor;
	}

	//使用业务角色
	private BizRoleDescriptor usedRole;

	//业务状态变化
	private BizStateSwitchDesc bizStateSwitchDesc;

	
	/**
	 * @return the usedRoles
	 */
	public BizRoleDescriptor getUsedRole() {
		return usedRole;
	}
	/**
	 * @param usedRoles the usedRoles to set
	 */
	public void setUsedRole(BizRoleDescriptor usedRole) {
		this.usedRole = usedRole;
	}
	
	
	/**
	 * @return the BizStateSwitchDesc
	 */
	public BizStateSwitchDesc getBizStateSwitch() {
		return bizStateSwitchDesc;
	}
	/**
	 * @param BizStateSwitchDesc the BizStateSwitchDesc to set
	 */
	public void setBizStateSwitch(BizStateSwitchDesc bizStateSwitchDesc) {
		this.bizStateSwitchDesc = bizStateSwitchDesc;
	}
	
	
	//构造
	public ActionDescriptor(){
		
	}
	
	public ActionDescriptor(HandlerDescriptor handlerDescriptor, BizRoleDescriptor role, BizStateSwitchDesc bizStateSwitchDesc) {
		setHandlerDescriptor(handlerDescriptor);
		setUsedRole(role);
		setBizStateSwitch(bizStateSwitchDesc);

	}
	
}
