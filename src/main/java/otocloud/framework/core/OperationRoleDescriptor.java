/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
//运行时业务角色
public class OperationRoleDescriptor {

	/**
	 * Constructor.
	 *
	 * @param operationRoleId
	 * @param operationRoleName
	 */
	public OperationRoleDescriptor(String operationRoleId,
			String operationRoleName) {
		super();
		this.operationRoleId = operationRoleId;
		this.operationRoleName = operationRoleName;
	}
	/**
	 * @return the operationRoleId
	 */
	public String getOperationRoleId() {
		return operationRoleId;
	}
	/**
	 * @param operationRoleId the operationRoleId to set
	 */
	public void setOperationRoleId(String operationRoleId) {
		this.operationRoleId = operationRoleId;
	}
	/**
	 * @return the operationRoleName
	 */
	public String getOperationRoleName() {
		return operationRoleName;
	}
	/**
	 * @param operationRoleName the operationRoleName to set
	 */
	public void setOperationRoleName(String operationRoleName) {
		this.operationRoleName = operationRoleName;
	}
	//操作员角色
	private String operationRoleId;	
	private String operationRoleName;	


	
}
