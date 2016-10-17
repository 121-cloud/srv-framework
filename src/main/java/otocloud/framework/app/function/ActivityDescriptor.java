/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import java.util.List;

import otocloud.framework.core.OtoCloudEventDescriptor;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月21日
 * @author lijing@yonyou.com
 */
public class ActivityDescriptor {
	//活动前缀
	private String activityName;
	/**
	 * @return the activityPrefix
	 */
	public String getActivityName() {
		return activityName;
	}
	/**
	 * @param activityPrefix the activityPrefix to set
	 */
	public void setActivityName(String activityName) {
		this.activityName = activityName;
	}
	
	private String bizObjectType;
	/**
	 * @return the bizObjectType
	 */
	public String getBizObjectType() {
		return bizObjectType;
	}
	/**
	 * @param bizObjectType the bizObjectType to set
	 */
	public void setBizObjectType(String bizObjectType) {
		this.bizObjectType = bizObjectType;
	}

	//业务角色
	private List<BizRoleDescriptor> bizRolesDesc;
	//业务操作
	private List<ActionDescriptor> actionsDesc;
	//业务事件
	private List<OtoCloudEventDescriptor> bizEventsDesc;
	/**
	 * @return the bizEventDesc
	 */
	public List<OtoCloudEventDescriptor> getBizEventsDesc() {
		return bizEventsDesc;
	}
	/**
	 * @param bizEventDesc the bizEventDesc to set
	 */
	public void setBizEventsDesc(List<OtoCloudEventDescriptor> bizEventsDesc) {
		this.bizEventsDesc = bizEventsDesc;
	}
	/**
	 * @return the bizRolesDesc
	 */
	public List<BizRoleDescriptor> getBizRolesDesc() {
		return bizRolesDesc;
	}
	/**
	 * @param bizRolesDesc the bizRolesDesc to set
	 */
	public void setBizRolesDesc(List<BizRoleDescriptor> bizRolesDesc) {
		this.bizRolesDesc = bizRolesDesc;
	}
	/**
	 * @return the actionsDesc
	 */
	public List<ActionDescriptor> getActionsDesc() {
		return actionsDesc;
	}
	/**
	 * @param actionsDesc the actionsDesc to set
	 */
	public void setActionsDesc(List<ActionDescriptor> actionsDesc) {
		this.actionsDesc = actionsDesc;
	}

	//构造
	public ActivityDescriptor(String bizObjectType, String activityName, 
			List<BizRoleDescriptor> bizRolesDesc, List<ActionDescriptor> actionsDesc, List<OtoCloudEventDescriptor> bizEventDesc){
		setBizRolesDesc(bizRolesDesc);
		setActionsDesc(actionsDesc);
		setActionsDesc(actionsDesc);
		setActivityName(activityName);
		setBizObjectType(bizObjectType);		
	}

}
