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
//invitee:{account,app,from_role,to_role}
public class MessageActor {
	
	/**
	 * Constructor.
	 *
	 * @param account
	 * @param app
	 * @param fromRole
	 * @param toRole
	 * @param rolerelation
	 * @param appInst
	 */
	public MessageActor(String account, String app, String fromRole,
			String toRole) {
		super();
		this.account = account;
		this.app = app;
		this.fromRole = fromRole;
		this.toRole = toRole;
	}

	public MessageActor(){
		
	}
	
	public MessageActor(String account, String app) {		
		this.account = account;
		this.app = app;
	}

	/**
	 * @return the account
	 */
	public String getAccount() {
		return account;
	}
	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	/**
	 * @return the app
	 */
	public String getApp() {
		return app;
	}
	/**
	 * @param app the app to set
	 */
	public void setApp(String app) {
		this.app = app;
	}
	/**
	 * @return the fromRole
	 */
	public String getFromRole() {
		return fromRole;
	}
	/**
	 * @param fromRole the fromRole to set
	 */
	public void setFromRole(String fromRole) {
		this.fromRole = fromRole;
	}
	/**
	 * @return the toRole
	 */
	public String getToRole() {
		return toRole;
	}
	/**
	 * @param toRole the toRole to set
	 */
	public void setToRole(String toRole) {
		this.toRole = toRole;
	}
	
	protected String account = "";
	protected String app = "";
	protected String fromRole = "";
	protected String toRole = "";
	protected String rolerelation = "";
	protected String appInst = "";
	
	/**
	 * @return the rolerelation
	 */
	public String getRolerelation() {
		return rolerelation;
	}


	/**
	 * @param rolerelation the rolerelation to set
	 */
	public void setRolerelation(String rolerelation) {
		this.rolerelation = rolerelation;
	}
	
	/**
	 * @return the appInst
	 */
	public String getAppInst() {
		return appInst;
	}
	/**
	 * @param appInst the appInst to set
	 */
	public void setAppInst(String appInst) {
		this.appInst = appInst;
	}


	public JsonObject toJsonObject(){
		JsonObject ret = new JsonObject()
		.put("account", account)
		.put("app", app)
		.put("app_inst", appInst)
		.put("from_role", fromRole)
		.put("to_role", toRole)
		.put("role_relation", rolerelation);
		
		return ret;
	}
	
	public void fromJsonObject(JsonObject msgAct){
		this.setAccount(msgAct.getString("account"));		
		this.setApp(msgAct.getString("app"));
		if(msgAct.containsKey("app_inst"))	
			this.setAppInst(msgAct.getString("app_inst"));
		if(msgAct.containsKey("from_role"))	
			this.setFromRole(msgAct.getString("from_role"));
		if(msgAct.containsKey("to_role"))	
			this.setToRole(msgAct.getString("to_role"));
		if(msgAct.containsKey("role_relation"))	
			this.setRolerelation(msgAct.getString("role_relation"));

	}
	

	
}
