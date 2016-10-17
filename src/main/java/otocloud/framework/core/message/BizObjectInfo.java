/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.message;

import io.vertx.core.json.JsonObject;




/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
/*{bo_type:<消息ID>，bo_id:,current_status:}*/	
public class BizObjectInfo
{
	public BizObjectInfo(){
		
	}

	/**
	 * Constructor.
	 *
	 * @param bizObjectType
	 * @param bizObjectId
	 * @param currentStatus
	 */
	public BizObjectInfo(String bizObjectType, String bizObjectId, String currentStatus) {
		super();
		this.bizObjectType = bizObjectType;
		this.bizObjectId = bizObjectId;
		this.currentStatus = currentStatus;
	}

	private String bizObjectType;
	private String bizObjectId;
	private String currentStatus;
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

	/**
	 * @return the bizObjectId
	 */
	public String getBizObjectId() {
		return bizObjectId;
	}

	/**
	 * @param bizObjectId the bizObjectId to set
	 */
	public void setBizObjectId(String bizObjectId) {
		this.bizObjectId = bizObjectId;
	}

	/**
	 * @return the currentStatus
	 */
	public String getCurrentStatus() {
		return currentStatus;
	}

	/**
	 * @param currentStatus the currentStatus to set
	 */
	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public JsonObject toJsonObject(){
		JsonObject ret = new JsonObject();
		ret.put("bo_type", bizObjectType);
		ret.put("bo_id", bizObjectId);
		ret.put("current_status", currentStatus);
		
		return ret;
	}
	
	
	public void fromJsonObject(JsonObject msgObj){
		this.bizObjectType = msgObj.getString("bo_type");
		this.bizObjectId = msgObj.getString("bo_id");	
		if(msgObj.containsKey("current_status"))
			this.currentStatus = msgObj.getString("current_status");		
	}
}
