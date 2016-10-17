/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月29日
 * @author lijing@yonyou.com
 */
public class ActivityThread {
	public ActivityThread(){
		
	}
	
	/**
	 * Constructor.
	 *
	 * @param activity
	 * @param bizObjectType
	 * @param bizObjectId
	 */
	public ActivityThread(String activity, String bizObjectType,
			String bizObjectId) {
		super();
		this.activity = activity;
		this.bizObjectType = bizObjectType;
		this.bizObjectId = bizObjectId;
	}

	/**
	 * @return the activity
	 */
	public String getActivity() {
		return activity;
	}

	/**
	 * @param activity the activity to set
	 */
	public void setActivity(String activity) {
		this.activity = activity;
	}

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
	
	private String activity;
	
	private String bizObjectType;
	
	private String bizObjectId;
	
	
	public JsonObject toJsonObject(){
		JsonObject ret = new JsonObject();
		
		if(activity != null)
			ret.put("activity", activity);
		else
			ret.put("activity", "");
		
		ret.put("bo_type", bizObjectType);
		ret.put("bo_id", bizObjectId);
		
		return ret;
	}	

	public void fromJsonObject(JsonObject activityThread){
		if(activityThread.containsKey("activity"))
			this.activity = activityThread.getString("activity");
		else
			this.activity = "";
		
		this.bizObjectType = activityThread.getString("bo_type");			
		this.bizObjectId = activityThread.getString("bo_id");
	}
	

	
}
