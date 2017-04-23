/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.json.JsonObject;
import otocloud.framework.core.CommandMessage;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class BizObjectFindOneHandler extends ActionHandlerImpl<JsonObject> {

	//{appid}.{account}.bo.get
	public static final String BO_GET = "bo.get";

	
	/**
	 * Constructor.
	 *
	 * @param verticle
	 * @param eventAddress
	 */
	public BizObjectFindOneHandler(AppActivityImpl appActivity) {
		super(appActivity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(CommandMessage<JsonObject> msg) {    	

    	JsonObject msgBody = msg.body();
    	String boType = msgBody.getString("bo_type");
    	String boId = msgBody.getString("bo_id");
    	//String currentStatus = msgBody.getString("current_status");
    	JsonObject feilds = msgBody.getJsonObject("fields", null);
    	
    	this.queryLatestFactData(boType, boId, feilds, null, next->{
			  if (next.succeeded()) {	
				  JsonObject po = next.result();
				  msg.reply(po);
			  } else {
	    		  Throwable err = next.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  msg.fail(400, replyMsg);
			  }
			  
    		
    	});    	

       	
    }

	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return BO_GET;
	}
	

}
