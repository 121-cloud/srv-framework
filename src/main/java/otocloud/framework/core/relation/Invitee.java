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
//invitee:{account,app,from_role,to_role}
public class Invitee extends MessageActor {
	public Invitee(){
		
	}
	/**
	 * Constructor.
	 *
	 */
	public Invitee(String account, String app) {
		super(account, app);
	}

	private Boolean appReady = true;
	/**
	 * @return the appReady
	 */
	public Boolean getAppReady() {
		return appReady;
	}
	/**
	 * @param appReady the appReady to set
	 */
	public void setAppReady(Boolean appReady) {
		this.appReady = appReady;
	}


	@Override
	public JsonObject toJsonObject(){
		JsonObject ret = super.toJsonObject();
		ret.put("app_ready", appReady);
		
		return ret;
	}
	
	@Override
	public void fromJsonObject(JsonObject inviteeObj){
		super.fromJsonObject(inviteeObj);	

		if(inviteeObj.containsKey("app_ready"))
			this.setAppReady(inviteeObj.getBoolean("app_ready"));
	}

	
}
