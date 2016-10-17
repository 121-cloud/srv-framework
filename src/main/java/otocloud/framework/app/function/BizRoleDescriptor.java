/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
public class BizRoleDescriptor {
	private String bizRoleId;
	/**
	 * @return the bizRoleId
	 */
	public String getBizRoleId() {
		return bizRoleId;
	}
	/**
	 * @param bizRoleId the bizRoleId to set
	 */
	public void setBizRoleId(String bizRoleId) {
		this.bizRoleId = bizRoleId;
	}
	private String bizRoleName;
	/**
	 * @return the bizRoleName
	 */
	public String getBizRoleName() {
		return bizRoleName;
	}
	/**
	 * @param bizRoleName the bizRoleName to set
	 */
	public void setBizRoleName(String bizRoleName) {
		this.bizRoleName = bizRoleName;
	}
	
	//构造
	public BizRoleDescriptor(String bizRoleId, String bizRoleName){
		setBizRoleId(bizRoleId);
		setBizRoleName(bizRoleName);		
	}
	
}
