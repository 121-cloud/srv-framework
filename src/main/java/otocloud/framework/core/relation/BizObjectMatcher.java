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
public interface BizObjectMatcher {
	//匹配后的返回结果：{exist:<是否存在匹配对象>，confirmed:<匹配对象是否已确认>}
	void match(String sourceAccount, String sourceBizObjectType, JsonObject bizObject, String targetAccount, String targetBizObjectType,
			Handler<AsyncResult<JsonObject>> retHandler);
	
	void existTargetObject(String sourceAccount, String sourceBizObjectType, String sourceBizObjectId, 
			String targetAccount, String targetBizObjectType, Handler<AsyncResult<JsonObject>> retHandler);
	
	void recordMatchRelation(String sourceAccount, String sourceBizObjectType, String sourceBizObjectId, 
			String targetAccount, String targetBizObjectType, String targetBizObjectId, Handler<AsyncResult<JsonObject>> retHandler);
	
	
}
