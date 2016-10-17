/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import otocloud.framework.app.common.BoPagingCounts;
import otocloud.framework.app.common.PagingOptions;
import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudEventHandlerImpl;
import otocloud.framework.core.message.BizMessage;
import otocloud.framework.core.message.BizObjectInfo;
import otocloud.framework.core.message.MessageActor;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
//@SuppressWarnings("rawtypes")
public abstract class ActionHandlerImpl<T> extends OtoCloudEventHandlerImpl<T> implements ActionHandler<T>, FactDataRecorder {
	public static final String MONGO_BO_LATEST_STATE = "latest_state";
	//{appid}.{account}.bo.get
	public static final String BO_GET = "bo.get";	

	protected ActionDescriptor actionDesc;

	protected AppActivityImpl appActivity;		
	
	protected OtoCloudAppDataSource backgroundDatasource;
    
    public OtoCloudAppDataSource getBackgroundDatasource() {
		return backgroundDatasource;
	}


	public void setBackgroundDatasource(OtoCloudAppDataSource backgroundDatasource) {
		this.backgroundDatasource = backgroundDatasource;
	}
	
	public OtoCloudAppDataSource getCurrentDataSource(){
		if(backgroundDatasource != null)
			return backgroundDatasource;
		return appActivity.getAppDatasource();
	}


	public AppActivityImpl getAppActivity() {
		return appActivity;
	}


	public ActionHandlerImpl(AppActivityImpl appActivity) {
    	super(appActivity);
    	this.appActivity = appActivity;
    }
    
    
	@Override
	public ActionDescriptor getActionDesc() {	
    	if(actionDesc != null)
    		return actionDesc;
		HandlerDescriptor handlerDescriptor = getHanlderDesc();
		actionDesc = new ActionDescriptor(handlerDescriptor, null, null);
		return actionDesc;
	}	

    
/*	@Override
	public List<BizRole> getActionRolesForCurrentAccount() {
		List<BizRole> retBizRoles = new ArrayList<BizRole>();		
		AppInstanceContext context = appActivity.getAppInstContext();
		ActionDescriptor actionDescriptor = getActionDesc();		
		if(actionDescriptor != null){		
			List<BizRoleDescriptor> actionBizRoles = actionDescriptor.getUsedRoles();
			if(actionBizRoles != null && actionBizRoles.size() > 0){
				actionBizRoles.forEach(bizRoleDesc -> {
					List<BizRole> fromRoles = context.getFromBizRolesList();
					if(fromRoles != null && fromRoles.size() > 0){
						fromRoles.forEach(fromRole -> {
							if(fromRole.getBizRole() == bizRoleDesc.getBizRoleId()){
								retBizRoles.add(fromRole);							
							}						
						});					
					}
					List<BizRole> toRoles = context.getToBizRolesList();
					if(toRoles != null && toRoles.size() > 0){
						toRoles.forEach(toRole -> {
							if(toRole.getBizRole() == bizRoleDesc.getBizRoleId()){
								retBizRoles.add(toRole);							
							}						
						});					
					}				
				}); 
				
			}	
		}
		return retBizRoles;
	}    */
    
    @Override
    public void recordFactData(String bizObjectType, JsonObject factData,
			String boId, JsonObject actor, String partnerAcct, MongoClient mongoCli,
			Handler<AsyncResult<String>> next) {
		ActionDescriptor actionDesc = getActionDesc();
		BizStateSwitchDesc stateSwitchDesc = actionDesc.getBizStateSwitch();
		String preStatus = stateSwitchDesc.getFromState();
		String newState = stateSwitchDesc.getToState();

		recordFactData(bizObjectType, factData, boId, 
				preStatus, newState, stateSwitchDesc.needPublishEvent(), actor, partnerAcct, mongoCli, next);
    }
	
    /**
     * 1. 向当前状态记录表中，插入一条记录。
     * 2. 更新前一个状态记录表（更新字段：下一个状态）。
     * 3. 向最新状态表中，更新（或者插入）对应的一条记录。
     * TODO 事务处理
     */
	@Override
	public void recordFactData(String bizObjectType, JsonObject factData, String boId,
			String preState, String newState, boolean publishStateSwitchEvent, JsonObject actor, String partnerAcct, MongoClient mongoCli, Handler<AsyncResult<String>> next){
		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();  
		
		MongoClient mongoClient = mongoCliTemp;
    	
		Future<String> ret = Future.future();
		ret.setHandler(next);
		
 		String account = this.appActivity.getAppInstContext().getAccount();
 		//String boId = factData.getString("bo_id");
		
		JsonObject boData = new JsonObject();
		boData.put("bo_id", boId);
		boData.put("previous_state", preState);
		boData.put("current_state", newState);
		boData.put("next_state", "");
		boData.put("account", account);		
		boData.put("ts", getDate());		
		boData.put("actor", actor);
		if(factData != null){	
			boData.put("partner", partnerAcct);
			boData.put("bo", factData);
			// 1. 向当前状态记录表中，插入一条记录。
		    mongoClient.insert(getBoFactTableName(bizObjectType, newState), boData, res -> {
			  if (res.succeeded()) {
				  String id = res.result();
	
				  if (preState == null || preState.length() == 0) {
					  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
					  this.recordDataOfLatestState(bizObjectType, newState, boId, factData, id, mongoClient, ret);
				  } else {
					  // 2. 更新前一个状态记录表（更新字段：下一个状态）。
					  this.updateNextStateFiledOfPreviousTable(bizObjectType, preState, newState, factData, boId, id, mongoClient, ret);
				  }
				  if(publishStateSwitchEvent)
					  publishBizStateSwitchEvent(bizObjectType, boId, preState, newState,  actor);
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(err);
			  }		  
			});
		}else{
			queryFactData(bizObjectType, boId, preState, null, mongoClient, latestBoRet->{
				  if (latestBoRet.succeeded()) {
					  	JsonObject latestfactData = latestBoRet.result();
					  	if(latestfactData != null){	
					  		boData.put("partner", latestfactData.getString("partner"));
							boData.put("bo", latestfactData.getJsonObject("bo"));
							// 1. 向当前状态记录表中，插入一条记录。
						    mongoClient.insert(getBoFactTableName(bizObjectType, newState), boData, res -> {
							  if (res.succeeded()) {
								  String id = res.result();
					
								  if (preState == null || preState.length() == 0) {
									  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
									  this.recordDataOfLatestState(bizObjectType, newState, boId, latestfactData, id, mongoClient, ret);
								  } else {
									  // 2. 更新前一个状态记录表（更新字段：下一个状态）。
									  this.updateNextStateFiledOfPreviousTable(bizObjectType, preState, newState, latestfactData, boId, id, mongoClient, ret);
								  }
								  if(publishStateSwitchEvent)
									  publishBizStateSwitchEvent(bizObjectType, boId, preState, newState,  actor);
							  } else {
					    		  Throwable err = res.cause();
					    		  String replyMsg = err.getMessage();
					    		  appActivity.getLogger().error(replyMsg, err);
					    		  ret.fail(err);
							  }		  
							});
					  	}else{
					  		appActivity.getLogger().error("no id");
					  		ret.complete("no id");
					  	}					  	
					  
				  } else {
		    		  Throwable err = latestBoRet.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }	
			});	
			
		}
	}
	
	private void updateNextStateFiledOfPreviousTable(String bizObjectType, String perStatus, String bizStatus, JsonObject factData, String boId, 
			String id, MongoClient mongoClient, Future<String> ret) {	  	
 		//String tableName = bizObjectType + "_" + perStatus;
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();
 		//String account = this.appActivity.getAppInstContext().getAccount();

		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		String account = this.appActivity.getAppInstContext().getAccount();
		query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, query);
		
    	JsonObject update = new JsonObject();
    	update.put("$set", new JsonObject().put("next_state", bizStatus).put("ts", getDate()));
		mongoClient.updateCollection(getBoFactTableName(bizObjectType, perStatus), query, update, result -> {
			  String replyMsg = "ok";
			  if (result.succeeded()) {
				  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
				  this.recordDataOfLatestState(bizObjectType, bizStatus, boId, factData, id, mongoClient, ret);
			  } else {
	    		  Throwable err = result.cause();
	    		  replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(err);
			  }
			});
	}
	
	private String getDate() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		return df.format(date);
	}
	
	//@SuppressWarnings("unchecked")
	private void recordDataOfLatestState(String bizObjectType, String bizStatus, String boId, 
			JsonObject factData, String id, MongoClient mongoClient, Future<String> ret) {
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();  

		JsonObject boIdCond = new JsonObject();
		boIdCond.put("bo_id", boId);
		
		String account = this.appActivity.getAppInstContext().getAccount();
		JsonObject query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, boIdCond);

		//query.put("bo_type", bizObjectType);
    	
		mongoClient.find(getBoLatestTableName(bizObjectType), query, res -> {
			  if (res.succeeded()) {
				  List<JsonObject> resultList = res.result();
				  if (resultList != null && resultList.size() > 0) {
				    	JsonObject update = new JsonObject();
				    	update.put("$set", new JsonObject().put("latest_state", bizStatus).put("ts", getDate()));
						mongoClient.updateCollection(getBoLatestTableName(bizObjectType), query, update, result -> {
							  String replyMsg = "ok";
							  if (result.succeeded()) {
								  ret.complete(id);
							  } else {
					    		  Throwable err = result.cause();
					    		  replyMsg = err.getMessage();
					    		  appActivity.getLogger().error(replyMsg, err);
					    		  ret.fail(err);
							  }
							});
				  } else {
				    	JsonObject insert = new JsonObject();
				    	insert.put("latest_state", bizStatus);
				    	insert.put("account", account);
				    	insert.put("bo_id", boId);
				    	insert.put("bo_type", bizObjectType);				    	
				    	
				    	//跟踪业务上下游线索
				        /*"biz_thread" : {
				            "root_bo_type" : "po",
				            "root_bo_id" : "PO001",
				            "previous_bo" : [ 
				                {
				                    "bo_type" : "rn",
				                    "bo_id" : "rn001"
				                }, 
				                {
				                    "bo_type" : "qs",
				                    "bo_id" : "qs001"
				                }
				            ]
				        }*/
				    	if(factData.containsKey("biz_thread")){
				    		insert.put("biz_thread", factData.getJsonObject("biz_thread").copy());				    		
				    	}
				    	
				    	insert.put("ts", getDate());
				    	mongoClient.insert(getBoLatestTableName(bizObjectType), insert, result -> {
						  String replyMsg = "ok";
						  if (result.succeeded()) {
							  ret.complete(id);
						  } else {
				    		  Throwable err = result.cause();
				    		  replyMsg = err.getMessage();
				    		  appActivity.getLogger().error(replyMsg, err);
				    		  ret.fail(err);
						  }
						});
				  }
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(err);
			  }		  
			});
	}
	
/*	@Override
	public void recordFactDatas(String bizObjectType, List<JsonObject> factDatas, JsonObject actor, Handler<AsyncResult<List<String>>> next){
		ActionDescriptor actionDesc = getActionDesc();
		BizStateSwitchDesc stateSwitchDesc = actionDesc.getBizStateSwitch();
		String preState = stateSwitchDesc.getFromState();
		String newState = stateSwitchDesc.getToState();
		
		recordFactDatas(bizObjectType, factDatas, preState, newState, actor, next);
	}*/

    /**
     * 1. 向当前状态记录表中，插入记录。
     * 2. 更新前一个状态记录表（更新字段：下一个状态）。
     * 3. 向最新状态表中，更新（或者插入）对应的记录。
     * TODO 事务处理
     */
/*	@Override
	public void recordFactDatas(String bizObjectType, List<JsonObject> factDatas, String preState, String newState, JsonObject actor, Handler<AsyncResult<List<String>>> next){
		
    	MongoClient mongoClient = appActivity.getMongoClient();  	  	
 		//String tableName = bizObjectType + "_" + newState;
 		
		Future<List<String>> ret = Future.future();
		ret.setHandler(next);
		
 		String account = this.appActivity.getAppInstContext().getAccount();
		
    	AtomicInteger saveCount = new AtomicInteger(0);    	
    	Integer size = factDatas.size();
    	
    	List<String> resultIds = new ArrayList<String>();
    	
    	// 1. 向当前状态记录表中，插入记录。
    	factDatas.forEach(factData ->{   
     		String boId = factData.getString("bo_id"); 		
    		
    		JsonObject boData = new JsonObject();
    		boData.put("bo_id", boId);
    		boData.put("previous_state", preState);
    		boData.put("next_state", "");
    		boData.put("account", account);
    		boData.put("ts", getDate());
    		boData.put("bo", factData);
    		boData.put("actor", actor);
    	    mongoClient.insert(getBoFactTableName(bizObjectType, newState), factData, res -> {
    			  if (res.succeeded()) {
    				  String id = res.result();
    				  if (preState == null || preState.length() == 0) {
    					  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
    					  this.recordDataofLatestState(bizObjectType, newState, boId, ret, id);
    				  } else {
    					  // 2. 更新前一个状态记录表（更新字段：下一个状态）。
    					  this.updateNextStateFiledofPreviousTable(bizObjectType, preState, newState, factData, boId, ret, id);;
    				  }
    				  publishBizStateSwitchEvent(bizObjectType, boId, preState, newState,  actor);
    			  } else {
    	    		  Throwable err = res.cause();
    	    		  String replyMsg = err.getMessage();
    	    		  appActivity.getLogger().error(replyMsg, err);    	    		  
    			  }	
    			  if(saveCount.incrementAndGet() == size){
    				  ret.complete(resultIds);
    		      }    			  
    			});
    	});
	}*/
	
	@Override
	public void queryLatestFactData(String bizObjectType, String boId, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next){
		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		
		String account = this.appActivity.getAppInstContext().getAccount();
		query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, query);
   	
		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();		
		MongoClient mongoClient = mongoCliTemp;

		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();     
		
		if(fields == null){
			mongoClient.find(getBoLatestTableName(bizObjectType), query, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  if (resultList != null && resultList.size() > 0) {
						  String latestState = resultList.get(0).getString("latest_state");
						  queryFactData(bizObjectType, boId, latestState, fields, mongoClient, next);
					  }else{
							Future<JsonObject> ret = Future.future();
							ret.setHandler(next);
							ret.complete(null);
					  }
				  }else{
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  
						Future<JsonObject> ret = Future.future();
						ret.setHandler(next);
						ret.fail(err);
				  }
			});
		}else{
			FindOptions findOptions = new FindOptions();	
			findOptions.setFields(fields);			

			mongoClient.findWithOptions(getBoLatestTableName(bizObjectType), query, findOptions, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  if (resultList != null && resultList.size() > 0) {
						  String latestState = resultList.get(0).getString("latest_state");
						  queryFactData(bizObjectType, boId, latestState, fields, mongoClient, next);
					  }else{
							Future<JsonObject> ret = Future.future();
							ret.setHandler(next);
							ret.complete(null);
					  }
				  }else{
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  
						Future<JsonObject> ret = Future.future();
						ret.setHandler(next);
						ret.fail(err);
				  }
			});
			
			
			
		}
	}

	@Override
	public void queryFactData(String bizObjectType, String boId, String boStatus, JsonObject fields, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next){

		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		
		String account = this.appActivity.getAppInstContext().getAccount();
		query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, query);

		
		//String tbName = bizObjectType + "_" + boStatus;
		
		Future<JsonObject> ret = Future.future();
		ret.setHandler(next);
		
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();   
		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();		
		MongoClient mongoClient = mongoCliTemp;

		
		if(fields == null){
			mongoClient.find(getBoFactTableName(bizObjectType, boStatus), query, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  if (resultList != null && resultList.size() > 0) {
						  ret.complete(resultList.get(0));
					  } else {
						  ret.complete(null);					  
					  }	
				  }else{  
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }		  
				});
		}else{
			FindOptions findOptions = new FindOptions();	
			findOptions.setFields(fields);			

			mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatus), query, findOptions, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  if (resultList != null && resultList.size() > 0) {
						  ret.complete(resultList.get(0));
					  } else {
						  ret.complete(null);					  
					  }	
				  }else{  
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }		  
				});

			
		}
	}
	
	@Override
	public void queryFactDataList(String bizObjectType, String boStatus, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next){
		
		Future<List<JsonObject>> ret = Future.future();
		ret.setHandler(next);
		
		String account = this.appActivity.getAppInstContext().getAccount();
		JsonObject query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, queryCondition);
		
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();
		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();		
		MongoClient mongoClient = mongoCliTemp;

		
		if(fields == null){
			mongoClient.find(getBoFactTableName(bizObjectType, boStatus), query, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  ret.complete(resultList);
				  }else{  
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }		  
				});		
		}else{
			
			FindOptions findOptions = new FindOptions();	
			findOptions.setFields(fields);			

			mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatus), query, findOptions, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  ret.complete(resultList);
				  }else{  
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }		  
				});		
		}
		
	}
	
	@Override
	public void queryFactDataList(String bizObjectType, String boStatus, PagingOptions pagingOptions, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next){
		String account = this.appActivity.getAppInstContext().getAccount();		
		pagingOptions.queryCond = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, pagingOptions.queryCond);
		
		Future<List<JsonObject>> ret = Future.future();
		ret.setHandler(next);
		
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();
		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();		
		MongoClient mongoClient = mongoCliTemp;

		mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatus), 
				pagingOptions.queryCond, pagingOptions.findOptions, findRet->{
					if (findRet.succeeded()) {															
						ret.complete(findRet.result());
					} else {
						Throwable err = findRet.cause();
						String errMsg = err.getMessage();
						appActivity.getLogger().error(errMsg, err);
						ret.fail(err);
					}
					
				});		
		
	}
	
	//查多状态值为反映最新状态，避免查历史状态，会将“next_state”不为空的过滤掉
	@Override
	public void queryFactDataList(String bizObjectType, List<String> boStatusList, JsonObject fields, JsonObject queryCondition, MongoClient mongoCli, Handler<AsyncResult<List<JsonObject>>> next){

		Future<List<JsonObject>> ret = Future.future();
		ret.setHandler(next);
		
		JsonObject retQuery = new JsonObject();
		if(queryCondition != null && queryCondition.size() > 0){
			retQuery.put("$and", new JsonArray()
											.add(new JsonObject().put("next_state", ""))
											.add(queryCondition));
		}else{
			retQuery.put("next_state", "");
		}

		
    	AtomicInteger count = new AtomicInteger(0);    	
    	Integer size = boStatusList.size();
    	
    	List<JsonObject> retList = Collections.synchronizedList(new ArrayList<JsonObject>());
    	
    	boStatusList.forEach(boStatus -> {
    		queryFactDataList(bizObjectType, boStatus, fields, retQuery, mongoCli, findRet->{
  			  if (findRet.succeeded()) {
					List<JsonObject> data = findRet.result();
					if(data.size() > 0){
						retList.addAll(data);
					}																		
  			  }else{
  	    		  Throwable err = findRet.cause();
  	    		  String replyMsg = err.getMessage();
  	    		  appActivity.getLogger().error(replyMsg, err);	    		 
  			  }
  			  if(count.incrementAndGet() == size){
  				 ret.complete(retList);				  
  			  }
    		});   		
			
       	});

		
		
		
	}


	@Override
	public void queryFactDataList(String bizObjectType, List<String> boStatusList, JsonObject fields, JsonObject paging, JsonObject otherCond, MongoClient mongoCli, Handler<AsyncResult<JsonObject>> next){

		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();		
		MongoClient mongoClient = mongoCliTemp;

		
		Future<JsonObject> ret = Future.future();
		ret.setHandler(next);
		
		JsonObject retQuery = new JsonObject();
		if(otherCond != null && otherCond.size() > 0){
			retQuery.put("$and", new JsonArray()
											.add(new JsonObject().put("next_state", ""))
											.add(otherCond));
		}else{
			retQuery.put("next_state", "");
		}
		
		String account = this.appActivity.getAppInstContext().getAccount();
		JsonObject queryCond = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, retQuery);
    	
		Integer pageSize = paging.getInteger("page_size");
    	long currentCount = paging.getInteger("page_number") * pageSize;
    	
		BoPagingCounts boPagingCounts = new BoPagingCounts();
		Future<BoPagingCounts> countRetFuture = Future.future();
		getFactDataTotalCount(0, bizObjectType, boStatusList, queryCond, boPagingCounts, mongoClient, countRetFuture);
		countRetFuture.setHandler(res->{
					if (res.succeeded()) {	

						Long totalNum = boPagingCounts.total.get();
						
						long newCurrentCount = currentCount;
						if(totalNum < currentCount){
							newCurrentCount = totalNum;
						}
						
						int tempTotalPage = (int)(totalNum/pageSize);
						if(totalNum % pageSize > 0){
							tempTotalPage = tempTotalPage + 1;
						}
						int totalPageCount = tempTotalPage;
						
						if(pageSize > totalNum){
							queryFactDataList(bizObjectType, boStatusList, fields, otherCond, mongoClient, findRet->{
								if (findRet.succeeded()) {															
									JsonArray retArray = new JsonArray(findRet.result());
									JsonObject retData = new JsonObject()
										.put("total", totalNum)
										.put("total_page", totalPageCount)
										.put("datas", retArray);
									
									ret.complete(retData);
								} else {
									Throwable err = findRet.cause();
									String errMsg = err.getMessage();
									appActivity.getLogger().error(errMsg, err);
									ret.fail(err);
								}
							});
							return;
						}
						
						if(boPagingCounts.subTotals.get(0) >= newCurrentCount){	
							
							PagingOptions pagingOptions = PagingOptions.buildPagingOptions(fields, paging, queryCond);
							
							//MongoClient mongoClient = getCurrentDataSource().getMongoClient();    	
							mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatusList.get(0)), 
									pagingOptions.queryCond, pagingOptions.findOptions, findRet->{
										if (findRet.succeeded()) {															
											JsonArray retArray = new JsonArray(findRet.result());
											JsonObject retData = new JsonObject()
												.put("total", totalNum)
												.put("total_page", totalPageCount)
												.put("datas", retArray);
											
											ret.complete(retData);
										} else {
											Throwable err = findRet.cause();
											String errMsg = err.getMessage();
											appActivity.getLogger().error(errMsg, err);
											ret.fail(err);
										}
										
									});		
						}else{		
							String sortField = paging.getString("sort_field");
							Integer sortDirection = paging.getInteger("sort_direction");
							JsonObject sortBson = new JsonObject().put(sortField, sortDirection);
							
							FindOptions startPagingOptions = new FindOptions();								
							startPagingOptions.setSort(sortBson);
							
							FindOptions endPagingOptions = new FindOptions();							
							endPagingOptions.setSort(sortBson);
							
							if(fields != null){
								startPagingOptions.setFields(fields);
								endPagingOptions.setFields(fields);
							}
							
							int startIndex = getStartIndex(boPagingCounts, paging, startPagingOptions);							
							int endIndex = getEndIndex(boPagingCounts, paging, endPagingOptions);
							
							if(endIndex == -1){
								
							}else if(startIndex == endIndex){
								startPagingOptions.setLimit(pageSize);
							}
							
							//MongoClient mongoClient = getCurrentDataSource().getMongoClient();    	
							mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatusList.get(startIndex)), 
									queryCond, startPagingOptions, findRet->{
										if (findRet.succeeded()) {		
											List<JsonObject> retLst = findRet.result();
											
											if(endIndex == -1 || startIndex == endIndex){
												JsonArray retArray = new JsonArray(retLst);
												JsonObject retData = new JsonObject()
													.put("total", totalNum)
													.put("total_page", totalPageCount)
													.put("datas", retArray);
												
												ret.complete(retData);
												
											}else if(startIndex + 1 == endIndex){
												mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatusList.get(endIndex)), 
														queryCond, endPagingOptions, endFindRet->{
															if (endFindRet.succeeded()) {		
																List<JsonObject> endRetLst = endFindRet.result();
																if(endRetLst.size() > 0){
																	retLst.addAll(endRetLst);
																}																		
	
															}else{
																Throwable err = endFindRet.cause();
																String errMsg = err.getMessage();
																appActivity.getLogger().error(errMsg, err);
															}
															
															JsonArray retArray = new JsonArray(retLst);
															JsonObject retData = new JsonObject()
																.put("total", totalNum)
																.put("total_page", totalPageCount)
																.put("datas", retArray);
															
															ret.complete(retData);
															
														});
												
												
											}else{
												Future<Void> middleDataFt = Future.future();
												getMiddleData(startIndex + 1, endIndex - 1, boStatusList, bizObjectType, paging, fields, queryCond, retLst, mongoClient, middleDataFt);
												middleDataFt.setHandler(middleDataRet->{
													if(middleDataRet.succeeded()){
														
														mongoClient.findWithOptions(getBoFactTableName(bizObjectType, boStatusList.get(endIndex)), 
																queryCond, endPagingOptions, endFindRet->{
																	if (endFindRet.succeeded()) {		
																		List<JsonObject> endRetLst = endFindRet.result();
																		if(endRetLst.size() > 0){
																			retLst.addAll(endRetLst);
																		}																		
	
																	}else{
																		Throwable err = endFindRet.cause();
																		String errMsg = err.getMessage();
																		appActivity.getLogger().error(errMsg, err);
																	}
																	
																	JsonArray retArray = new JsonArray(retLst);
																	JsonObject retData = new JsonObject()
																		.put("total", totalNum)
																		.put("total_page", totalPageCount)
																		.put("datas", retArray);
																	
																	ret.complete(retData);
																	
																});
	
														
													}else{
														
														Throwable err = middleDataRet.cause();
														String errMsg = err.getMessage();
														appActivity.getLogger().error(errMsg, err);
														
														JsonArray retArray = new JsonArray(retLst);
														JsonObject retData = new JsonObject()
															.put("total", totalNum)
															.put("total_page", totalPageCount)
															.put("datas", retArray);
														
														ret.complete(retData);
														
													}
													
												});
												
											}
										} else {
											Throwable err = findRet.cause();
											String errMsg = err.getMessage();
											appActivity.getLogger().error(errMsg, err);
											ret.fail(err);
										}
										
									});		
	
							
						}
												
					} else {
						Throwable err = res.cause();
						String errMsg = err.getMessage();
						appActivity.getLogger().error(errMsg, err);
						ret.fail(err);
					}
					
				});			

		
	}
	
	
	private void getMiddleData(Integer index, Integer endIdx, List<String> boStatusList, String bizObjectType, JsonObject paging, JsonObject fields, JsonObject otherCond, List<JsonObject> retList, MongoClient mongoCli, Future<Void> next){
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();  
		
		String sortField = paging.getString("sort_field");
		Integer sortDirection = paging.getInteger("sort_direction");
		
		FindOptions findOptions = new FindOptions();	
		JsonObject sortBson = new JsonObject().put(sortField, sortDirection);
		findOptions.setSort(sortBson);
		if(fields != null){
			findOptions.setFields(fields);
		}

		mongoCli.findWithOptions(getBoFactTableName(bizObjectType, boStatusList.get(index)), 
				otherCond, findOptions, findRet->{
			if (findRet.succeeded()) {	
				List<JsonObject> ret = findRet.result();
				retList.addAll(ret);

			} else {
				Throwable err = findRet.cause();
				String errMsg = err.getMessage();
				appActivity.getLogger().error(errMsg, err);
				
			}
			Integer newIdx = index + 1;
			if(newIdx > endIdx){
				next.complete();
			}else{
				getMiddleData(newIdx, endIdx, boStatusList, bizObjectType, paging, fields, otherCond, retList, mongoCli, next);
			}			
			
		});		
	}
	
	
	private int getStartIndex(BoPagingCounts boPagingCounts, JsonObject paging, FindOptions ret){
		int startIndex = (paging.getInteger("page_number")-1) * paging.getInteger("page_size");
		int count = 0;
		for(int i=0;i<boPagingCounts.subTotals.size();i++){
			if(count + boPagingCounts.subTotals.get(i) >= startIndex){
				//PagingOptions ret = PagingOptions.buildPagingOptions(paging, otherCond);
				int skip = startIndex - count;
				ret.setSkip(skip);
				//ret.setLimit(boPagingCounts.subTotals.get(i) - skip);				
				return i;
			}else{
				count = count + boPagingCounts.subTotals.get(i);
			}
		}
		return -1;
	}
	
	private int getEndIndex(BoPagingCounts boPagingCounts, JsonObject paging, FindOptions ret){
		int endIndex = paging.getInteger("page_number") * paging.getInteger("page_size");
		int count = 0;
		for(int i=0;i<boPagingCounts.subTotals.size();i++){
			if(count + boPagingCounts.subTotals.get(i) >= endIndex){
				//PagingOptions ret = PagingOptions.buildPagingOptions(paging, otherCond);
				int limit = endIndex - count;
				ret.setSkip(0);
				ret.setLimit(limit);				
				return i;
			}else{
				count = count + boPagingCounts.subTotals.get(i);
			}
		}
		return -1;
	}
	
	private void getFactDataTotalCount(int index, String bizObjectType, List<String> boStatusList, JsonObject queryCond, BoPagingCounts boPagingCounts, MongoClient mongoCli, Future<BoPagingCounts> next){
	
		String boStatus = boStatusList.get(index);
		
		getFactDataCount(bizObjectType, boStatus, queryCond, mongoCli, countRet->{
			  if (countRet.succeeded()) {
				  Integer retValue = Integer.valueOf(countRet.result().toString());
				  boPagingCounts.subTotals.add(retValue);
				  boPagingCounts.total.addAndGet(retValue);
			  }else{
	    		  Throwable err = countRet.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);	    		 
			  }
			  int newIndex = index + 1;
			  if(newIndex >= boStatusList.size()){
				  next.complete(boPagingCounts);				  
			  }else{
				  getFactDataTotalCount(newIndex, bizObjectType, boStatusList, queryCond, boPagingCounts, mongoCli, next);
			  }
  		});   		
    	
	}	
	
	public void existFactData(String bizObjectType, String boId, MongoClient mongoCli, Handler<AsyncResult<Boolean>> next){
		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		
		String account = this.appActivity.getAppInstContext().getAccount();
		query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, query);
		
		Future<Boolean> ret = Future.future();
		ret.setHandler(next);
    	
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();      	
		mongoCli.find(getBoLatestTableName(bizObjectType), query, res -> {
			  if (res.succeeded()) {
				  List<JsonObject> resultList = res.result();
				  if (resultList != null && resultList.size() > 0) {
					  ret.complete(true);
				  }else{
					  ret.complete(false);
				  }
			  }else{
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(replyMsg);
			  }
		});
		
	}
	
	@Override
	public void getFactDataCount(String bizObjectType, String boStatus, JsonObject queryCond, MongoClient mongoCli, Handler<AsyncResult<Long>> next){
		String account = this.appActivity.getAppInstContext().getAccount();
		JsonObject query = getCurrentDataSource().getDataPersistentPolicy().getQueryConditionForMongo(account, queryCond);
		
		Future<Long> ret = Future.future();
		ret.setHandler(next);
		
		MongoClient mongoCliTemp = mongoCli;
		if(mongoCli == null)
			mongoCliTemp = getCurrentDataSource().getMongoClient();		
		MongoClient mongoClient = mongoCliTemp;
    	
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();      	
		mongoClient.count(getBoFactTableName(bizObjectType, boStatus), query, res -> {
			  if (res.succeeded()) {
				  Long size = res.result();				  
				  ret.complete(size);
			  }else{
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(replyMsg);
			  }
		});		
	}
	
	
	public void getPartnerBizObjectByMessage(BizMessage bizMessage,
			Handler<AsyncResult<JsonObject>> bizObjectRet) {
		
		Future<JsonObject> ret = Future.future();
		ret.setHandler(bizObjectRet);			
		
	   	MessageActor sender = bizMessage.getSender();	
		
		BizObjectInfo bizObjInfo = bizMessage.getBizObjectInfo();	
		
		String getAddress = sender.getApp() + "." + sender.getAccount()+ "." + BO_GET;
		
		JsonObject retMsgObj = new JsonObject()
			.put("bo_id", bizObjInfo.getBizObjectId())
			.put("bo_type", bizObjInfo.getBizObjectType());


		appActivity.getEventBus().<JsonObject>send(getAddress, retMsgObj, reply->{
			if (reply.succeeded()) {
				Message<JsonObject> retMsg = reply.result();
				if(retMsg != null)
					ret.complete(retMsg.body());
				else {
					ret.complete();
				}
			}else{
	    		  Throwable err = reply.cause();
  	    		  String replyMsg = err.getMessage();
  	    		  appActivity.getLogger().error(replyMsg, err);
  	    		  ret.fail(err);
			}			
			
		});
		
		
	}
	
	@Override
	public void publishBizStateSwitchEvent(String bizObjType, String bizObjId, String preState, String newState, JsonObject actor) {    	
		
		String stateSwitchEventAddress = BizStateSwitchDesc.buildStateSwitchEventAddress(bizObjType, preState, newState);	
		String targetAddress = appActivity.buildEventAddress(stateSwitchEventAddress);
		
		BizStateChangedMessage msg = buildBizStateChangedMessage(bizObjType, bizObjId, preState, newState);
		
		JsonObject sendData = msg.toJsonObject();				  
		//发布状态变化事件
		appActivity.getEventBus().publish(targetAddress, sendData);	
		
		//记录日志
		appActivity.getLogger().info(actor, "发布状态变化事件" + targetAddress + ",业务对象：" + bizObjId);
	}
	
	//派生类可重写
	public BizStateChangedMessage buildBizStateChangedMessage(String bizObjType, String boId, String previousStatus, String currentStatus) {
		return new BizStateChangedMessage(appActivity.getService().getRealServiceName(), 
				appActivity.getAppInstContext().getAccount(), bizObjType,
				boId, previousStatus, currentStatus);
	}

	
	public String getDBTableName(String tableName){
		String account = this.appActivity.getAppInstContext().getAccount();	
		return getCurrentDataSource().getDBTableName(account, tableName);
	}
	
	public String getBoFactTableName(String boType, String boStatus){
		String tableName = boType + "_" + boStatus;		
		return getDBTableName(tableName);
	}	
	
	public String getBoFactTableName(String boStatus){
		String boType = this.appActivity.getBizObjectType();
		String tableName = boType + "_" + boStatus;		
		return getDBTableName(tableName);
	}	
	
	public String getBoLatestTableName(String boType){
		String boLatestTb = boType + "_" + MONGO_BO_LATEST_STATE;
		return getDBTableName(boLatestTb);
	}
	
	public String getBoLatestTableName(){
		String boType = this.appActivity.getBizObjectType();
		String boLatestTb = boType + "_" + MONGO_BO_LATEST_STATE;
		return getDBTableName(boLatestTb);
	}
	
}