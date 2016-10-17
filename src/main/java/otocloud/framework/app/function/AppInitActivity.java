/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月12日
 * @author lijing@yonyou.com
 */
public interface AppInitActivity {
	AppInitCmdHandlerImpl createInitCmdHandler();
	void notifyAppInitCompleted(String appId, String account, Handler<AsyncResult<Boolean>> nextHandler);
}
