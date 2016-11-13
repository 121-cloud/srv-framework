/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import java.util.List;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;



/**
 * TODO: 业务线索跟踪器
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface BizThreadRecorder{ 	
	
	//开始根业务
	void bizRootStart(String rootObjectType, String rootObjectId, Handler<AsyncResult<Void>> next);
	
	//结束根业务
	void bizRootEnd(String rootObjectType, String rootObjectId, Handler<AsyncResult<Void>> next);
	
	//记录业务活动开始
	void bizActivityStart(String rootObjectType, String rootObjectId, JsonArray previousActivities,
			String currentBoId,	Handler<AsyncResult<Void>> next);
/*	void bizActivityStart(String rootObjectType, String rootObjectId, List<ActivityThread> previousActivities,
			ActivityThread currentActivity, Handler<AsyncResult<Void>> next);
	void bizActivityStart(String rootObjectType, String rootObjectId, JsonObject bizThread,
			ActivityThread currentActivity, Handler<AsyncResult<Void>> next);*/
	
	//记录业务活动完成
	void bizActivityEnd(String rootObjectType, String rootObjectId, String currentBoId, Handler<AsyncResult<Void>> next);
	
	//向下跟踪业务线索
    void trackBizThreadForBoId(String boId, Handler<AsyncResult<List<JsonObject>>> result);
    
	//查询正在进行的业务活动
    void queryHotBizActivities(String rootObjectType, String rootObjectId, Handler<AsyncResult<List<ActivityThread>>> result);
	
}