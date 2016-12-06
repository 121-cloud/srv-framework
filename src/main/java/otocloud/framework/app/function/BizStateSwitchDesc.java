/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
public class BizStateSwitchDesc {
	//private Class bizRoot; //暂不实现强类型
	//private String bizRoot;
	private BizRootType bizRootType = BizRootType.BIZ_OBJECT;
	private String fromState;
	private String toState;
	private boolean containsFactData = false;
	private boolean needPublishEvent = false;
	private Boolean webExpose = false;
	
	public boolean isContainsFactData() {
		return containsFactData;
	}
	public void setContainsFactData(boolean containsFactData) {
		this.containsFactData = containsFactData;
	}
	/**
	 * @return the webExpose
	 */
	public Boolean getWebExpose() {
		return webExpose;
	}
	/**
	 * @param webExpose the webExpose to set
	 */
	public void setWebExpose(Boolean webExpose) {
		this.webExpose = webExpose;
	}
	
	public boolean needPublishEvent() {
		return needPublishEvent;
	}
	public void setNeedPublishEvent(boolean needPublishEvent) {
		this.needPublishEvent = needPublishEvent;
	}
	
	/**
	 * @return the bizRoot
	 */
/*	public String getBizRoot() {
		return bizRoot;
	}
	*//**
	 * @param bizRoot the bizRoot to set
	 *//*
	public void setBizRoot(String bizRoot) {
		this.bizRoot = bizRoot;
	}*/
	/**
	 * @return the bizRootType
	 */
	public BizRootType getBizRootType() {
		return bizRootType;
	}
	/**
	 * @param bizRootType the bizRootType to set
	 */
	public void setBizRootType(BizRootType bizRootType) {
		this.bizRootType = bizRootType;
	}
	/**
	 * @return the fromState
	 */
	public String getFromState() {
		return fromState;
	}
	/**
	 * @param fromState the fromState to set
	 */
	public void setFromState(String fromState) {
		this.fromState = fromState;
	}
	/**
	 * @return the toState
	 */
	public String getToState() {
		return toState;
	}
	/**
	 * @param toState the toState to set
	 */
	public void setToState(String toState) {
		this.toState = toState;
	}

	//构造
	public BizStateSwitchDesc(BizRootType bizRootType, String fromState, String toState){
		//setBizRoot(bizRoot);
		setBizRootType(bizRootType);
		setFromState(fromState);
		setToState(toState);		
	}
	
	public BizStateSwitchDesc(BizRootType bizRootType, String fromState, String toState, boolean needPublishEvent, boolean containsFactData){
		//setBizRoot(bizRoot);
		setBizRootType(bizRootType);
		setFromState(fromState);
		setToState(toState);	
		setNeedPublishEvent(needPublishEvent);
		setContainsFactData(containsFactData);
	}
	
	public BizStateSwitchDesc(BizRootType bizRootType, String fromState, String toState, boolean containsFactData){
		//setBizRoot(bizRoot);
		setBizRootType(bizRootType);
		setFromState(fromState);
		setToState(toState);	
		setContainsFactData(containsFactData);
	}
	
	public static String buildStateSwitchEventAddress(String bizObjType, String preState, String newState){
		String srcState = preState;
		if(preState == null || preState.isEmpty()){
			srcState = "unknown";
		}
		return String.format("bo_statuschanged.%s.%s_%s", bizObjType, srcState, newState);
	}
	
	

}
