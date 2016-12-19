/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import otocloud.framework.app.common.BizRoleDirection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;




/**
 * TODO: 事实数据记录器
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface CDORecorder extends FactDataRecorder{   
	
	JsonObject buildStubForCDO(JsonObject factData, String boId, String partnerAcct);
	
	void recordCDO(BizRoleDirection roleDirection, String partnerAcct, JsonObject factData, String boId, JsonObject actor, Handler<AsyncResult<String>> next);
	void recordCDO(BizRoleDirection roleDirection, String partnerAcct,  String bizObjectType, JsonObject factData, String boId, JsonObject actor, Handler<AsyncResult<String>> next);
	void recordCDO(BizRoleDirection roleDirection, String partnerAcct,  String bizObjectType, JsonObject factData, String boId, String preState, String newState, boolean publishStateSwitchEvent, boolean containsFactData, JsonObject actor, Handler<AsyncResult<String>> next);
	
    /**
     * TODO 更新最新状态数据，不触发状态变化事件
     */	
	void updateLatestCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject factData, String boId,			
			JsonObject actor, Handler<AsyncResult<String>> next);
	void updateCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject query, JsonObject update, String status,		
			JsonObject actor, Handler<AsyncResult<JsonObject>> next);
	void updateCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject factData, String boId,	String status,		
			JsonObject actor, Handler<AsyncResult<String>> next);
	
     //查询单个BO的最终状态数据
	void queryLatestCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, String boId, JsonObject fields, Handler<AsyncResult<JsonObject>> next);
    //查询单个BO的指定状态数据
	void queryCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, String boId, String boStatus, JsonObject fields, Handler<AsyncResult<JsonObject>> next);


}