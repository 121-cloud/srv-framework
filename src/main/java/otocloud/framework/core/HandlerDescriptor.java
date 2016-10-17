/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import java.util.List;

import otocloud.common.ActionURI;




/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月29日
 * @author lijing@yonyou.com
 */
public class HandlerDescriptor {
	private String apiName;
	//处理器地址
	private OtoCloudEventDescriptor handlerAddress;
	//参数名
	private List<ApiParameterDescriptor> paramsDesc;	
	//操作员角色
	private OperationRoleDescriptor operationRoleDescriptor;
	
	private ActionURI restApiURI;	
	
	private String messageFormat = "default";
	
	public String getMessageFormat() {
		return messageFormat;
	}
	public void setMessageFormat(String messageFormat) {
		this.messageFormat = messageFormat;
	}
	public String getApiName() {
		return apiName;
	}
	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public ActionURI getRestApiURI() {
		return restApiURI;
	}
	public void setRestApiURI(ActionURI restApiURI) {
		this.restApiURI = restApiURI;
	}
	/**
	 * @return the paramNames
	 */
	public List<ApiParameterDescriptor> getParamsDesc() {
		return paramsDesc;
	}
	/**
	 * @param params the paramNames to set
	 */
	public void setParamsDesc(List<ApiParameterDescriptor> paramsDesc) {
		this.paramsDesc = paramsDesc;
	}
	
	/**
	 * @return the handlerAddress
	 */
	public OtoCloudEventDescriptor getHandlerAddress() {
		return handlerAddress;
	}
	/**
	 * @param ebAddress the ebAddress to set
	 */
	public void setHandlerAddress(OtoCloudEventDescriptor ebAddress) {
		this.handlerAddress = ebAddress;
	}

	
	/**
	 * @return the operationRoleDescriptor
	 */
	public OperationRoleDescriptor getOperationRoleDescriptor() {
		return operationRoleDescriptor;
	}
	/**
	 * @param operationRoleDescriptor the operationRoleDescriptor to set
	 */
	public void setOperationRoleDescriptor(
			OperationRoleDescriptor operationRoleDescriptor) {
		this.operationRoleDescriptor = operationRoleDescriptor;
	}
	
	//构造
	public HandlerDescriptor(){
		
	}
	
	public HandlerDescriptor(String apiName, OtoCloudEventDescriptor handlerAddress, List<ApiParameterDescriptor> paramsDesc, OperationRoleDescriptor operationRole, ActionURI restApiURI) {
		setApiName(apiName);
		setHandlerAddress(handlerAddress);
		setParamsDesc(paramsDesc);
		setOperationRoleDescriptor(operationRole);
		setRestApiURI(restApiURI);
		
	}
	
}
