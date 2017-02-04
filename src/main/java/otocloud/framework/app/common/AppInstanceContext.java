/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.common;

import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public class AppInstanceContext {
/*	public static final String TO_BIZROLE_KEY = "tobizrole";
	public static final String From_BIZROLE_KEY = "frombizrole";
*/	
	//private String bizRoleId;
	
	/**
	 * @return the masterBizRoleId
	 */
/*	public String getBizRoleId() {
		return bizRoleId;
	}*/
	/**
	 * @param masterBizRoleId the masterBizRoleId to set
	 */
/*	public void setBizRoleId(String bizRoleId) {
		this.bizRoleId = bizRoleId;
	}*/

/*	private String instId;
	*//**
	 * @return the instId
	 *//*
	public String getInstId() {
		return instId;
	}*/
	/**
	 * @param instId the instId to set
	 */
/*	public void setInstId(String instId) {
		this.instId = instId;
	}

	private String instName;
	
	*//**
	 * @return the instName
	 *//*
	public String getInstName() {
		return instName;
	}*/
	/**
	 * @param instName the instName to set
	 */
/*	public void setInstName(String instName) {
		this.instName = instName;
	}*/
	
	private String appVersion;

	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	private String appId;
	/**
	 * @return the appId
	 */
	public String getAppId() {
		return appId;
	}
	/**
	 * @param appId the appId to set
	 */
	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	private String appDesc;
	
	/**
	 * @return the appDesc
	 */
	public String getAppDesc() {
		return appDesc;
	}
	/**
	 * @param appDesc the appDesc to set
	 */
	public void setAppDesc(String appDesc) {
		this.appDesc = appDesc;
	}

	private String account;

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}
	
/*	private JsonArray fromBizRoles;
	*//**
	 * @return the fromBizRoles
	 *//*
	public JsonArray getFromBizRoles() {
		return fromBizRoles;
	}
	*//**
	 * @param fromBizRoles the fromBizRoles to set
	 *//*
	public void setFromBizRoles(JsonArray fromBizRoles) {
		this.fromBizRoles = fromBizRoles;
		fromBizRolesList = null;
	}*/
	
/*	private List<BizRole> fromBizRolesList;
	//将json角色数据转换为角色对象
	public List<BizRole> getFromBizRolesList(){
		if(fromBizRolesList != null)
			return fromBizRolesList;
		if(fromBizRoles != null && fromBizRoles.size() > 0){
			fromBizRolesList = new ArrayList<BizRole>();
			fromBizRoles.forEach(fromBizRole -> {
				JsonObject roleJs = (JsonObject)fromBizRole;
				BizRole role = new BizRole(roleJs.getString(From_BIZROLE_KEY), "");
				fromBizRolesList.add(role);
			});
		}
		return fromBizRolesList;
	}
	
	private JsonArray toBizRoles;
	*//**
	 * @return the toBizRoles
	 *//*
	public JsonArray getToBizRoles() {
		return toBizRoles;
	}
	
	private List<BizRole> toBizRolesList;
	//将json角色数据转换为角色对象
	public List<BizRole> getToBizRolesList(){
		if(toBizRolesList != null)
			return toBizRolesList;
		if(toBizRoles != null && toBizRoles.size() > 0){
			toBizRolesList = new ArrayList<BizRole>();
			toBizRoles.forEach(toBizRole -> {
				JsonObject roleJs = (JsonObject)toBizRole;
				BizRole role = new BizRole(roleJs.getString(TO_BIZROLE_KEY), "");
				toBizRolesList.add(role);
			});
		}
		return toBizRolesList;
	}*/
	/**
	 * @param toBizRoles the toBizRoles to set
	 */
/*	public void setToBizRoles(JsonArray toBizRoles) {
		this.toBizRoles = toBizRoles;
		toBizRolesList = null;
	}*/
	/**
	 * @return the bizRoles
	 */

/*	private JsonObject appInstCfg;
	*//**
	 * @return the appInstCfg
	 *//*
	public JsonObject getAppInstCfg() {
		return appInstCfg;
	}
	*//**
	 * @param appInstCfg the appInstCfg to set
	 *//*
	public void setAppInstCfg(JsonObject appInstCfg) {
		this.appInstCfg = appInstCfg;
	}*/
	
	public AppInstanceContext() {		
	}
	
	public AppInstanceContext(/*String appInstId, String appInstName, */ String appId, String appVersion, String appDescription,
			String account /* String bizRoleId *//*JsonArray fromBizRoles, JsonArray toBizRoles*/) {	
/*		this.setInstId(appInstId);
		this.setInstName(appInstName);*/
		this.setAppId(appId);
		this.setAppVersion(appVersion);
//		this.setServiceName(serviceName);
		this.setAppDesc(appDescription);
		this.setAccount(account);
		//this.setBizRoleId(bizRoleId);
/*		this.setFromBizRoles(fromBizRoles);
		this.setToBizRoles(toBizRoles);*/
		//appCfg.put("account", acccode);
//		this.setAppInstCfg(appCfg);		
	}
	
	public AppInstanceContext(JsonObject appCtx) {	
		this.fromJson(appCtx);
	}
	
	public JsonObject toJson(){
		JsonObject ret = new JsonObject();
		ret/*.put("instId", instId)
		   .put("instName", instName)*/
		   .put("appId", appId)
		   .put("appVersion", appVersion)
//		   .put("serviceName", serviceName)
		   .put("appDesc", appDesc)
		   .put("account",account);
		   //.put("bizRoleId", bizRoleId);
/*		   .put("fromBizRoles", fromBizRoles)
		   .put("toBizRoles", toBizRoles);*/
		return ret;
	}
	
	public void fromJson(JsonObject appCtx) {
/*		this.setInstId(appCtx.getString("instId"));
		this.setInstName(appCtx.getString("instName"));*/
		this.setAppId(appCtx.getString("appId"));
//		this.setServiceName(appCtx.getString("serviceName"));
		this.setAppDesc(appCtx.getString("appDesc"));
		this.setAccount(appCtx.getString("account"));
		this.setAppVersion(appCtx.getString("appVersion"));
/*		this.setFromBizRoles(appCtx.getJsonArray("fromBizRoles"));
		this.setToBizRoles(appCtx.getJsonArray("toBizRoles"));*/
	}
	
}
