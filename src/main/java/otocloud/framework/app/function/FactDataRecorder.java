/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import java.util.List;

import io.vertx.ext.mongo.MongoClient;
import otocloud.framework.app.common.PagingOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;




/**
 * TODO: 事实数据记录器
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface FactDataRecorder{   
	
	void recordFactData(JsonObject factData, String boId, JsonObject actor, String partnerAcct,	Handler<AsyncResult<String>> next);
	void recordFactData(String bizObjectType, JsonObject factData, String boId, JsonObject actor, String partnerAcct, MongoClient mongoCli, Handler<AsyncResult<String>> next);
	void recordFactData(String bizObjectType, JsonObject factData, String boId, String preState, String newState, boolean publishStateSwitchEvent, boolean containsFactData, JsonObject actor, String partnerAcct, MongoClient mongoCli, Handler<AsyncResult<String>> next);
	
    /**
     * TODO 更新最新状态数据，不触发状态变化事件
     */	
	void updateLatestFactData(String bizObjectType, JsonObject factData, String boId,			
			JsonObject actor, MongoClient mongoCli, Handler<AsyncResult<String>> next);
    /**
     * TODO 更新指定状态数据，不触发状态变化事件
     */	
	void updateFactData(String bizObjectType, JsonObject factData, String boId,	String status,		
			JsonObject actor, MongoClient mongoCli, Handler<AsyncResult<String>> next);
    
	void existFactData(String bizObjectType, String boId, MongoClient mongoCli, Handler<AsyncResult<Boolean>> next);
    //查询单个BO的最终状态数据
	void queryLatestFactData(String bizObjectType, String boId, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);
    //查询单个BO的指定状态数据
	void queryFactData(String bizObjectType, String boId, String boStatus, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);
    //查询某状态BO数量
	void getFactDataCount(String bizObjectType, String boStatus, JsonObject queryCond, MongoClient mongoCli, Handler<AsyncResult<Long>> next);
    //查询单个BO的所有状态数据
	void queryAllStatusDataForBO(String bizObjectType, String boId, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);
	
    //分页查询指定状态BO集合（条件和字段在分页对象中指定）
    void queryFactDataList(String bizObjectType, String boStatus, PagingOptions pagingOptions, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);
    //按条件查询指定状态BO集合（不分页）
    void queryFactDataList(String bizObjectType, String boStatus, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);

    //指定多状态查询最新状态数据（不分页）
    void queryLatestFactDataList(String bizObjectType, List<String> boStatusList, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);
    //指定多状态查询最新状态数据（分页）
    void queryLatestFactDataList(String bizObjectType, List<String> boStatusList, JsonObject fields, JsonObject paging, JsonObject otherCond, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);

    //按指定单一状态查询最新状态数据（不分页）
    void queryLatestFactDataList(String bizObjectType, String boStatus, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);
    //按指定单一状态查询最新状态数据（分页）
    void queryLatestFactDataList(String bizObjectType, String boStatus, JsonObject fields, JsonObject paging, JsonObject otherCond, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);

    //指定单一状态查询最新状态数据（分页）
    void queryLatestFactDataList(String bizObjectType, String boStatus, JsonObject pagingInfo, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);

    //指定多状态查询最新状态数据（分页）
    void queryLatestFactDataList(String bizObjectType, List<String> boStatusList, JsonObject pagingInfo, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);

    
    //发布状态变化事件
    void publishBizStateSwitchEvent(String bizObjType, String bizObjId, String preState, String newState, JsonObject actor, JsonObject factData);
    
    //构建状态变化消息，派生类可重写
  	BizStateChangedMessage buildBizStateChangedMessage(String bizObjType, String boId, String previousStatus, String currentStatus, JsonObject factData);
	
}