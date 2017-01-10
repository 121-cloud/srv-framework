/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import otocloud.common.ActionURI;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月29日
 * @author lijing@yonyou.com
 */
public class RestActionDescriptor {
	private String componentName;
	private ActionURI actionRUI;
	private HandlerDescriptor actionDesc;
	
	public String getComponentName() {
		return componentName;
	}
	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	
	/**
	 * @return the actionRUI
	 */
	public ActionURI getActionURI() {
		return actionRUI;
	}
	/**
	 * @param actionRUI the actionRUI to set
	 */
	public void setActionURI(ActionURI actionRUI) {
		this.actionRUI = actionRUI;
	}


	/**
	 * @return the actionDesc
	 */
	public HandlerDescriptor getActonDesc() {
		return actionDesc;
	}
	/**
	 * @param actionDesc the actionDesc to set
	 */
	public void setActionDesc(HandlerDescriptor actionDesc) {
		this.actionDesc = actionDesc;
	}
	
	//构造
	public RestActionDescriptor(ActionURI actionRUI, HandlerDescriptor actionDesc) {
		setActionURI(actionRUI);
		setActionDesc(actionDesc);		
	}
	
	public RestActionDescriptor(HandlerDescriptor actionDesc) {		
		setActionDesc(actionDesc);
		
	}
	
}
