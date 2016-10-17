/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import otocloud.common.ActionURI;
import otocloud.framework.app.function.ActionDescriptor;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月29日
 * @author lijing@yonyou.com
 */
public class RestActionDescriptor {
	private ActionURI actionRUI;
	
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

	private ActionDescriptor actionDesc;
	
	/**
	 * @return the actionDesc
	 */
	public ActionDescriptor getActonDesc() {
		return actionDesc;
	}
	/**
	 * @param actionDesc the actionDesc to set
	 */
	public void setActionDesc(ActionDescriptor actionDesc) {
		this.actionDesc = actionDesc;
	}
	
	//构造
	public RestActionDescriptor(ActionURI actionRUI, ActionDescriptor actionDesc) {
		setActionDesc(actionDesc);		
	}
	
	public RestActionDescriptor(ActionDescriptor actionDesc) {		
		setActionDesc(actionDesc);
		
	}
	
}
