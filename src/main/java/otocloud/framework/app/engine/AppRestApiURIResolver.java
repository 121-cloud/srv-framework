/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import otocloud.framework.core.OtoCloudBusMessage;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME! 
 * @date 2015�?7�?1�?
 * @author lijing@yonyou.com
 */
public class AppRestApiURIResolver extends AppServiceEngineHandlerImpl<JsonObject> {
	
	public static final String ADDRESS = "platform.container.uri_resolver";

	/**
	 * Constructor.
	 *
	 * @param appServiceEngine
	 */
	public AppRestApiURIResolver(AppServiceEngineImpl appServiceEngine) {
		super(appServiceEngine);
	}

	@Override
	public void handle(OtoCloudBusMessage<JsonObject> msg) {
		JsonObject body = msg.body();
		
		this.appServiceEngine.getLogger().info("API地址转换请求:" + body.toString());
		
		JsonObject session = body.getJsonObject(otocloud.common.webserver.MessageBodyConvention.SESSION, null);
		if(session == null){
			msg.reply(new JsonObject().put("realAddress", body.getString("addressPattern")));
			return;
		}		
		Integer account = session.getInteger(otocloud.common.webserver.MessageBodyConvention.SESSION_ACCT_ID, -1);
		if(account == -1){
			msg.reply(new JsonObject().put("realAddress", body.getString("addressPattern")));
			return;
		}
		
/*		JsonObject retMsg = new JsonObject()
		.put("realAddress", appServiceEngine.getServiceName() + "." + account + "." + body.getString("addressPattern"));
*/
		
		JsonObject retMsg = new JsonObject()
			.put("realAddress", account.toString() + "." + body.getString("addressPattern"));
		
		msg.reply(retMsg);
    }

	@Override
	public String getEventAddress() {
		return ADDRESS;
	}
	
}
