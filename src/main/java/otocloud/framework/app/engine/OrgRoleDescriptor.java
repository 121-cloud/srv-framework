/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
public class OrgRoleDescriptor {
	private String orgRoleId;
	/**
	 * @return the bizRoleId
	 */
	public String getOrgRoleId() {
		return orgRoleId;
	}
	/**
	 * @param bizRoleId the bizRoleId to set
	 */
	public void setOrgRoleId(String orgRoleId) {
		this.orgRoleId = orgRoleId;
	}
	private String orgRoleName;
	/**
	 * @return the orgRoleName
	 */
	public String getOrgRoleName() {
		return orgRoleName;
	}
	/**
	 * @param orgRoleName the bizRoleName to set
	 */
	public void setOrgRoleName(String orgRoleName) {
		this.orgRoleName = orgRoleName;
	}
	
	//构造
	public OrgRoleDescriptor(String orgRoleId, String orgRoleName){
		setOrgRoleId(orgRoleId);
		setOrgRoleName(orgRoleName);		
	}
	
}
