/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import otocloud.framework.core.OtoCloudEventHandler;
import otocloud.framework.core.message.BizMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;




/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface ActionHandler<T> extends ActionHandlerRegistry, OtoCloudEventHandler<T>, FactDataRecorder {   
   
    void getPartnerBizObjectByMessage(BizMessage bizMessage, Handler<AsyncResult<JsonObject>> bizObjectRet);
  	
/*	void getAppStatus(Handler<AsyncResult<String>> next);
	void appEnabled(Handler<AsyncResult<Boolean>> next);
	void appInitialized(Handler<AsyncResult<Boolean>> next);*/
	
}