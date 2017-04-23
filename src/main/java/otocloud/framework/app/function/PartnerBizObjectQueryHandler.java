/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;


import io.vertx.core.json.JsonObject;
import otocloud.framework.core.CommandMessage;
import otocloud.framework.core.message.BizMessage;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年7月1日
 * @author lijing@yonyou.com
 */
public class PartnerBizObjectQueryHandler extends ActionHandlerImpl<JsonObject> {

	public static final String LISTEN_BO_QUERY_BASE = "partner.bo.query";		
	
	/**
	 * Constructor.
	 *
	 * @param verticle
	 * @param eventAddress
	 */
	public PartnerBizObjectQueryHandler(AppActivityImpl appActivity) {
		super(appActivity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(CommandMessage<JsonObject> msg) {    	

		JsonObject body = msg.body();
    	
		BizMessage msgObj = new BizMessage();
		msgObj.fromJsonObject(body);
		
		this.getPartnerBizObjectByMessage(msgObj, retHandle->{
			  if (retHandle.succeeded()) {
				  msg.reply(retHandle.result());
			  } else {
	    		  Throwable err = retHandle.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  msg.fail(400, replyMsg);
			  }
			
		});

    }

	@Override
	public String getEventAddress() {
		// TODO Auto-generated method stub
		return LISTEN_BO_QUERY_BASE;
	} 


}
