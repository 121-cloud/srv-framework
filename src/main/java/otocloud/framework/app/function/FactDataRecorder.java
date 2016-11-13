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
	
	void recordFactData(String bizObjectType, JsonObject factData, String boId, JsonObject actor, String partnerAcct, MongoClient mongoCli, Handler<AsyncResult<String>> next);
	void recordFactData(String bizObjectType, JsonObject factData, String boId, String preState, String newState, boolean publishStateSwitchEvent, JsonObject actor, String partnerAcct, MongoClient mongoCli, Handler<AsyncResult<String>> next);
	//void recordFactDatas(String bizObjectType, List<JsonObject> factDatas, JsonObject actor, Handler<AsyncResult<List<String>>> next);
	//void recordFactDatas(String bizObjectType, List<JsonObject> factDatas, String preState, String newState, JsonObject actor, Handler<AsyncResult<List<String>>> next);
    
	void existFactData(String bizObjectType, String boId, MongoClient mongoCli, Handler<AsyncResult<Boolean>> next);
    void queryLatestFactData(String bizObjectType, String boId, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);
    void queryFactData(String bizObjectType, String boId, String boStatus, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);
    void getFactDataCount(String bizObjectType, String boStatus, JsonObject queryCond, MongoClient mongoCli, Handler<AsyncResult<Long>> next);
    
    void queryFactDataList(String bizObjectType, String boStatus, PagingOptions pagingOptions, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);
    void queryFactDataList(String bizObjectType, String boStatus, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);
    void queryFactDataList(String bizObjectType, List<String> boStatusList, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next);
    void queryFactDataList(String bizObjectType, List<String> boStatusList, JsonObject fields, JsonObject paging, JsonObject otherCond, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next);
    //发布状态变化事件
    void publishBizStateSwitchEvent(String bizObjType, String bizObjId, String preState, String newState, JsonObject actor);
    
    //构建状态变化消息，派生类可重写
  	BizStateChangedMessage buildBizStateChangedMessage(String bizObjType, String boId, String previousStatus, String currentStatus);
	
}