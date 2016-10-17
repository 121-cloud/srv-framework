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
public class ApiParameterDescriptor {

	public ApiParameterDescriptor(String parameterName, String parameterType) {
		super();
		this.parameterName = parameterName;
		this.parameterType = parameterType;
	}
	public String getParameterName() {
		return parameterName;
	}
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}
	public String getParameterType() {
		return parameterType;
	}
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}
	//操作员角色
	private String parameterName;	
	private String parameterType;	


	
}
