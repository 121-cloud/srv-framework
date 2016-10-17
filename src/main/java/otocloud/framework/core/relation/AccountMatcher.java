/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.relation;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年7月30日
 * @author lijing@yonyou.com
 */
public interface AccountMatcher {
	void match(JsonObject bizRelaObject,
			Handler<AsyncResult<JsonObject>> retHandler);
}
