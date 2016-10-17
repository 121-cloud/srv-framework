/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.relation;

import otocloud.framework.core.message.MessageActor;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
//sponsor:{account,app,app_inst,from_role,to_role}
public class Sponsor extends MessageActor {
	public Sponsor(){
		
	}
	/**
	 * Constructor.
	 *
	 * @param account
	 * @param app
	 * @param appInst
	 * @param fromRole
	 * @param toRole
	 */
	public Sponsor(String account, String app, String appInst, String fromRole,
			String toRole, String bizUnitRoleDataType, String toRoleBizUnitId) {	
		super(account, app);
		this.appInst = appInst;
		this.fromRole = fromRole;
		this.toRole = toRole;
		this.toRoleBizUnitId = toRoleBizUnitId;
		this.bizUnitRoleDataType = bizUnitRoleDataType;
	}
	
	private String toRoleBizUnitId = "";	
	private String bizUnitRoleDataType = "";
	
	/**
	 * @return the bizUnitRoleDataType
	 */
	public String getBizUnitRoleDataType() {
		return bizUnitRoleDataType;
	}
	/**
	 * @param bizUnitRoleDataType the bizUnitRoleDataType to set
	 */
	public void setBizUnitRoleDataType(String bizUnitRoleDataType) {
		this.bizUnitRoleDataType = bizUnitRoleDataType;
	}
	/**
	 * @return the toRoleBizUnitId
	 */
	public String getToRoleBizUnitId() {
		return toRoleBizUnitId;
	}
	/**
	 * @param toRoleBizUnitId the toRoleBizUnitId to set
	 */
	public void setToRoleBizUnitId(String toRoleBizUnitId) {
		this.toRoleBizUnitId = toRoleBizUnitId;
	}
	
	@Override
	public JsonObject toJsonObject(){
		JsonObject ret = super.toJsonObject();
		ret.put("bu_roledata_type", bizUnitRoleDataType);
		ret.put("to_role_unit_Id", toRoleBizUnitId);
		
		return ret;
	}
	
	@Override
	public void fromJsonObject(JsonObject sponsorObj){
		super.fromJsonObject(sponsorObj);	
		
		if(sponsorObj.containsKey("bu_roledata_type"))
			this.setBizUnitRoleDataType(sponsorObj.getString("bu_roledata_type"));

		if(sponsorObj.containsKey("to_role_unit_Id"))
			this.setToRoleBizUnitId(sponsorObj.getString("to_role_unit_Id"));
	}

	
}
