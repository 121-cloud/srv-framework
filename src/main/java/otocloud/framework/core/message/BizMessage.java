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
/*{
	id:<消息ID>，
	sender:{account,app,app_inst,from_role,to_role},
	receiver:{account,app,from_role,to_role},
	message:<邀请消息正文>,
	bo_info:{bo_type:<消息ID>，bo_id:,current_status:},
	msgStatus：<消息状态>
}*/	
public class BizMessage extends OtoCloudMessage {
	
	public BizMessage(){		
		super();
	}

	public BizMessage(String id, MessageActor sender, MessageActor receiver, String message,
			BizObjectInfo bizObjectInfo, Integer msgStatus) {
		super(id, sender, receiver, message, msgStatus);
		this.bizObjectInfo = bizObjectInfo;
	}
	
	public BizMessage(MessageActor sender, MessageActor receiver, String message, BizObjectInfo bizObjectInfo,
			Integer msgStatus) {
		super(sender, receiver, message, msgStatus);
		this.bizObjectInfo = bizObjectInfo;
	}
	
	public BizMessage(MessageActor sender, MessageActor receiver, String message, BizObjectInfo bizObjectInfo){
		super(sender, receiver, message);
		this.bizObjectInfo = bizObjectInfo;
	}

	private BizObjectInfo bizObjectInfo;

	/**
	 * @return the bizObjectInfo
	 */
	public BizObjectInfo getBizObjectInfo() {
		return bizObjectInfo;
	}

	/**
	 * @param bizObjectInfo the bizObjectInfo to set
	 */
	public void setBizObjectInfo(BizObjectInfo bizObjectInfo) {
		this.bizObjectInfo = bizObjectInfo;
	}
	
	@Override
	public JsonObject toJsonObject(){
		JsonObject ret = super.toJsonObject();
		ret.put("bo_info", bizObjectInfo.toJsonObject());
		
		return ret;
	}
	
	@Override
	public JsonObject toJsonObjectForDB(){
		JsonObject ret = super.toJsonObjectForDB();
		ret.put("bo_info", bizObjectInfo.toJsonObject());
		
		return ret;
	}

	@Override
	public void fromJsonObject(JsonObject msgObj){
		super.fromJsonObject(msgObj);
		this.bizObjectInfo = new BizObjectInfo();
		this.bizObjectInfo.fromJsonObject(msgObj.getJsonObject("bo_info"));
	}
	
}
