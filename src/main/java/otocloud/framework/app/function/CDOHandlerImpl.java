/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

import otocloud.framework.app.common.BizRoleDirection;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
//@SuppressWarnings("rawtypes")
public abstract class CDOHandlerImpl<T> extends ActionHandlerImpl<T> implements CDORecorder {

	public CDOHandlerImpl(AppActivityImpl appActivity) {
    	super(appActivity);    	
    }
	
	@Override
    public void recordCDO(BizRoleDirection roleDirection, String partnerAcct, JsonObject factData, String boId, JsonObject actor, Handler<AsyncResult<String>> next) {
		ActionDescriptor actionDesc = getActionDesc();
		BizStateSwitchDesc stateSwitchDesc = actionDesc.getBizStateSwitch();
		String preStatus = stateSwitchDesc.getFromState();
		String newState = stateSwitchDesc.getToState();

		recordCDO(roleDirection, partnerAcct, this.appActivity.getBizObjectType(), factData, boId, 
				preStatus, newState, stateSwitchDesc.needPublishEvent(), stateSwitchDesc.isContainsFactData(), actor, next);
    }
    
	
	@Override
	public void recordCDO(BizRoleDirection roleDirection, String partnerAcct,  String bizObjectType, JsonObject factData, String boId, JsonObject actor, Handler<AsyncResult<String>> next){
	
		ActionDescriptor actionDesc = getActionDesc();
		BizStateSwitchDesc stateSwitchDesc = actionDesc.getBizStateSwitch();
		String preStatus = stateSwitchDesc.getFromState();
		String newState = stateSwitchDesc.getToState();

		recordCDO(roleDirection, partnerAcct, bizObjectType, factData, boId, 
				preStatus, newState, stateSwitchDesc.needPublishEvent(), stateSwitchDesc.isContainsFactData(), actor, next);
	}

	
	@Override
	public void recordCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject factData, String boId, String preState, String newState, boolean publishStateSwitchEvent, boolean containsFactData, JsonObject actor, Handler<AsyncResult<String>> next){
		
		MongoClient mongoClient = this.appActivity.getCDODatasource().getMongoClient();
    	
		Future<String> ret = Future.future();
		ret.setHandler(next);
		
 		String account = this.appActivity.getAppInstContext().getAccount();
		
		JsonObject boData = new JsonObject();
		
		boolean needCreateBoId = false;
		if(boId != null && !boId.isEmpty()){
			boData.put("bo_id", boId);
		}
		else {
			needCreateBoId = true;
		}
		final boolean _needCreateBoId = needCreateBoId;
		
		String fromAcctTemp;
		String toAcctTemp;
		if(roleDirection == BizRoleDirection.FROM){
			boData.put("from_account", account);
			boData.put("to_account", partnerAcct);
			boData.put("from_actor", actor);
			boData.put("update_direction", "from");
			fromAcctTemp = this.appActivity.getAppInstContext().getAccount();
			toAcctTemp = partnerAcct;
		}else {			
			boData.put("from_account", partnerAcct);
			boData.put("to_account", account);
			boData.put("to_actor", actor);
			boData.put("update_direction", "to");
			fromAcctTemp = partnerAcct;
			toAcctTemp = this.appActivity.getAppInstContext().getAccount();
		}
		final String fromAcct = fromAcctTemp;
		final String toAcct = toAcctTemp;
		
		boData.put("previous_state", preState);
		boData.put("current_state", newState);
		boData.put("next_state", "");
		
		boData.put("ts", getNowDate());	
		if(factData != null){	
			//boData.put("partner", partnerAcct);
			boData.put("bo", factData);
			// 1. 向当前状态记录表中，插入一条记录。
			
			if(preState != null && preState.equals(newState)){			
				if(_needCreateBoId){
			  		String errMsg = "bo_id is not null";
		    		appActivity.getLogger().error(errMsg);
		    		ret.fail(errMsg);
		    		return;
				}
				
				JsonObject query = new JsonObject();
				query.put("bo_id", boId);
				JsonObject update = new JsonObject();
				update.put("$set", boData);					

			    mongoClient.updateCollection(getCDOTableName(bizObjectType, newState, fromAcct, toAcct), query, update, res -> {
				  if (res.succeeded()) {
					  ret.complete(boId);
				  }else{
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }
			    });			
			
			}else{
				mongoClient.insert(getCDOTableName(bizObjectType, newState, fromAcct, toAcct), boData, res -> {
				  if (res.succeeded()) {
					  if(_needCreateBoId){ //新增时没有bo_id，则自动将_id作为bo_id					 
						 String _id = res.result();
						 JsonObject idCondObj = new JsonObject();
						 idCondObj.put("_id", _id);
						 JsonObject setCurrentBoId = new JsonObject();
						 setCurrentBoId.put("$set", new JsonObject().put("bo_id", _id).put("bo.bo_id", _id));
						 mongoClient.updateCollection(getCDOTableName(bizObjectType, newState, fromAcct, toAcct), idCondObj, setCurrentBoId, result -> {
							  if (result.succeeded()) {		
								  factData.put("bo_id", _id);
								  if (preState == null || preState.length() == 0) {
									  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
									  this.recordCDOforLatestState(roleDirection, fromAcct, toAcct, bizObjectType, preState, newState, _id, factData, mongoClient, ret);
								  } else {
									  // 2. 更新前一个状态记录表（更新字段：下一个状态）。
									  this.updateCDONextStateFiledOfPreviousTable(roleDirection, fromAcct, toAcct, bizObjectType, preState, newState, factData, _id, mongoClient, ret);
								  }
								  if(publishStateSwitchEvent){
									  JsonObject eventBO = null;
									  if(containsFactData)
										  eventBO = factData;
									  publishBizStateSwitchEvent(bizObjectType, _id, preState, newState, actor, eventBO);  
								  }
								  
							  } else {
					    		  Throwable err = result.cause();
					    		  String replyMsg = err.getMessage();
					    		  appActivity.getLogger().error(replyMsg, err);
					    		  ret.fail(err);
							  }
							});
					  }else{
		
						  if (preState == null || preState.length() == 0) {
							  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
							  this.recordCDOforLatestState(roleDirection, fromAcct, toAcct, bizObjectType, preState, newState, boId, factData, mongoClient, ret);
						  } else {
							  // 2. 更新前一个状态记录表（更新字段：下一个状态）。
							  this.updateCDONextStateFiledOfPreviousTable(roleDirection, fromAcct, toAcct, bizObjectType, preState, newState, factData, boId, mongoClient, ret);
						  }
						  if(publishStateSwitchEvent){
							  JsonObject eventBO = null;
							  if(containsFactData)
								  eventBO = factData;						  
							  publishBizStateSwitchEvent(bizObjectType, boId, preState, newState,  actor, eventBO);
						  }
					  }
				  } else {
		    		  Throwable err = res.cause();
		    		  String replyMsg = err.getMessage();
		    		  appActivity.getLogger().error(replyMsg, err);
		    		  ret.fail(err);
				  }		  
				});
			}
		}else{
			//查询前一状态数据
			queryCDO(roleDirection, partnerAcct, fromAcct, toAcct, bizObjectType, boId, preState, null, mongoClient, latestBoRet->{
				  if (latestBoRet.succeeded()) {
					  	JsonObject latestfactData = latestBoRet.result();
					  	if(latestfactData != null){	
					  		//boData.put("partner", latestfactData.getString("partner"));
							boData.put("bo", latestfactData.getJsonObject("bo"));
							
							final String preStateTmp = (preState == null || preState.isEmpty()) ? latestfactData.getString("current_state") : preState;
							
							boData.put("previous_state", preStateTmp);
							
							// 1. 向当前状态记录表中，插入一条记录。
						    mongoClient.insert(getCDOTableName(bizObjectType, newState, fromAcct, toAcct), boData, res -> {
							  if (res.succeeded()) {
								  //String id = res.result();					
								  if (preStateTmp == null || preStateTmp.length() == 0) {
									  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
									  this.recordCDOforLatestState(roleDirection, fromAcct, toAcct, bizObjectType, preStateTmp, newState, boId, latestfactData, mongoClient, ret);
								  } else {
									  // 2. 更新前一个状态记录表（更新字段：下一个状态）。
									  this.updateCDONextStateFiledOfPreviousTable(roleDirection, fromAcct, toAcct, bizObjectType, preStateTmp, newState, latestfactData, boId, mongoClient, ret);
								  }
								  if(publishStateSwitchEvent){
									  JsonObject eventBO = null;
									  if(containsFactData)
										  eventBO = factData;				  
									  publishBizStateSwitchEvent(bizObjectType, boId, preStateTmp, newState,  actor, eventBO);
								  }
							  } else {
					    		  Throwable err = res.cause();
					    		  String replyMsg = err.getMessage();
					    		  appActivity.getLogger().error(replyMsg, err);
					    		  ret.fail(err);
							  }		  
							});
					  	}else{
					  		String errMsg = "bo_id is not null";
					  		appActivity.getLogger().error(errMsg);
					  		ret.complete(errMsg);
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

    /**
     * TODO 更新最新状态数据，不触发状态变化事件
     */
	@Override
	public void updateLatestCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject factData, String boId,			
			JsonObject actor, Handler<AsyncResult<String>> next){
		MongoClient mongoClient = appActivity.getCDODatasource().getMongoClient();
    	
		Future<String> ret = Future.future();
		ret.setHandler(next);		
		
		JsonObject boData = new JsonObject();
		
		boData.put("ts", getNowDate());		
		//boData.put("actor", actor);
		boData.put("bo", factData);
		
		String fromAcctTemp;
		String toAcctTemp;
		if(roleDirection == BizRoleDirection.FROM){
			fromAcctTemp = this.appActivity.getAppInstContext().getAccount();
			toAcctTemp = partnerAcct;
			boData.put("from_actor", actor);
			boData.put("update_direction", "from");
		}else {			
			fromAcctTemp = partnerAcct;
			toAcctTemp = this.appActivity.getAppInstContext().getAccount();
			boData.put("to_actor", actor);
			boData.put("update_direction", "to");
		}
		final String fromAcct = fromAcctTemp;
		final String toAcct = toAcctTemp;		

		FindOptions findOptions = new FindOptions();	
		findOptions.setFields(new JsonObject().put("latest_state", true));		
		
		JsonObject query = new JsonObject();
		query.put("bo_id", boId);

		mongoClient.findWithOptions(getCDOLatestTableName(bizObjectType, fromAcct, toAcct), 
				query, findOptions, res -> {
			  if (res.succeeded()) {
				  List<JsonObject> resultList = res.result();
				  if (resultList != null && resultList.size() > 0) {
					  	String latestState = resultList.get(0).getString("latest_state");
					  
						JsonObject update = new JsonObject();
						update.put("$set", boData);					

					    mongoClient.updateCollection(getCDOTableName(bizObjectType, latestState, fromAcct, toAcct), query, update,
					    		updateRet -> {
						  if (updateRet.succeeded()) {
							  ret.complete(boId);
						  }else{
				    		  Throwable err = updateRet.cause();
				    		  String replyMsg = err.getMessage();
				    		  appActivity.getLogger().error(replyMsg, err);
				    		  ret.fail(err);
						  }
					    });			
					
				  }
			  }else{
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(err);
			  }
		});
	}
	
    /**
     * TODO 更新指定状态数据，不触发状态变化事件
     */
	@Override
	public void updateCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject query, JsonObject update, String status,		
			JsonObject actor, Handler<AsyncResult<JsonObject>> next){		
		
		MongoClient mongoClient = appActivity.getCDODatasource().getMongoClient();
    	
		Future<JsonObject> ret = Future.future();
		ret.setHandler(next);	
		
		String fromAcctTemp;
		String toAcctTemp;
		if(roleDirection == BizRoleDirection.FROM){
			fromAcctTemp = this.appActivity.getAppInstContext().getAccount();
			toAcctTemp = partnerAcct;
			update.put("from_actor", actor);
			update.put("update_direction", "from");
		}else {			
			fromAcctTemp = partnerAcct;
			toAcctTemp = this.appActivity.getAppInstContext().getAccount();
			update.put("to_actor", actor);
			update.put("update_direction", "to");
		}
		final String fromAcct = fromAcctTemp;
		final String toAcct = toAcctTemp;
		
		JsonObject updateJs = new JsonObject().put("$set", update);

	    mongoClient.updateCollection(getCDOTableName(bizObjectType, status, fromAcct, toAcct), query, updateJs,
	    		updateRet -> {
		  if (updateRet.succeeded()) {
			  ret.complete(updateRet.result().toJson());
		  }else{
    		  Throwable err = updateRet.cause();
    		  String replyMsg = err.getMessage();
    		  appActivity.getLogger().error(replyMsg, err);
    		  ret.fail(err);
		  }
	    });	
	}
	
    /**
     * TODO 更新指定状态数据，不触发状态变化事件
     */
	@Override
	public void updateCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, JsonObject factData, String boId,	String status,		
			JsonObject actor, Handler<AsyncResult<String>> next){
		MongoClient mongoClient = appActivity.getCDODatasource().getMongoClient();
    	
		Future<String> ret = Future.future();
		ret.setHandler(next);		
		
		JsonObject boData = new JsonObject();
		
		boData.put("ts", getNowDate());	
		boData.put("bo", factData);
		
		String fromAcctTemp;
		String toAcctTemp;
		if(roleDirection == BizRoleDirection.FROM){
			fromAcctTemp = this.appActivity.getAppInstContext().getAccount();
			toAcctTemp = partnerAcct;
			boData.put("from_actor", actor);
			boData.put("update_direction", "from");
		}else {			
			fromAcctTemp = partnerAcct;
			toAcctTemp = this.appActivity.getAppInstContext().getAccount();
			boData.put("to_actor", actor);
			boData.put("update_direction", "to");
		}
		final String fromAcct = fromAcctTemp;
		final String toAcct = toAcctTemp;		
		
		JsonObject query = new JsonObject();
		query.put("bo_id", boId);

					  
		JsonObject update = new JsonObject();
		update.put("$set", boData);					

	    mongoClient.updateCollection(getCDOTableName(bizObjectType, status, fromAcct, toAcct), query, update,
	    		updateRet -> {
		  if (updateRet.succeeded()) {
			  ret.complete(boId);
		  }else{
    		  Throwable err = updateRet.cause();
    		  String replyMsg = err.getMessage();
    		  appActivity.getLogger().error(replyMsg, err);
    		  ret.fail(err);
		  }
	    });	
	}
	
	private void updateCDONextStateFiledOfPreviousTable(BizRoleDirection roleDirection, String fromAcct,String toAcct, String bizObjectType, String perStatus, String bizStatus, JsonObject factData, String boId, 
			MongoClient mongoClient, Future<String> ret) {	  	
 		//String tableName = bizObjectType + "_" + perStatus;
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();
 		//String account = this.appActivity.getAppInstContext().getAccount();

		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		//String account = this.appActivity.getAppInstContext().getAccount();
		query = this.appActivity.getCDODatasource().getDataPersistentPolicy().getQueryConditionForMongo(fromAcct, toAcct, query);
		
    	JsonObject update = new JsonObject();
    	update.put("$set", new JsonObject().put("next_state", bizStatus).put("ts", getNowDate()));
		mongoClient.updateCollection(getCDOTableName(bizObjectType, perStatus, fromAcct, toAcct), query, update, result -> {
			  String replyMsg = "ok";
			  if (result.succeeded()) {
				  // 3. 向最新状态表中，更新（或者插入）对应的一条记录。
				  this.recordCDOforLatestState(roleDirection, fromAcct, toAcct, bizObjectType, perStatus, bizStatus, boId, factData, mongoClient, ret);
			  } else {
	    		  Throwable err = result.cause();
	    		  replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(err);
			  }
			});
	}

	
	private void recordCDOforLatestState(BizRoleDirection roleDirection, String fromAcct,String toAcct, String bizObjectType, String preStatus, String bizStatus, String boId, 
			JsonObject factData, MongoClient mongoClient, Future<String> ret) {
		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();  

		JsonObject boIdCond = new JsonObject();
		boIdCond.put("bo_id", boId);
		
		//String account = this.appActivity.getAppInstContext().getAccount();
		JsonObject query = this.appActivity.getCDODatasource().getDataPersistentPolicy().getQueryConditionForMongo(fromAcct, toAcct, boIdCond);
   	
		mongoClient.find(getCDOLatestTableName(bizObjectType, fromAcct, toAcct), query, res -> {
			  if (res.succeeded()) {
				  List<JsonObject> resultList = res.result();
				  if (resultList != null && resultList.size() > 0) {
					    if(preStatus != null && !preStatus.isEmpty()){
					    	//判断当前最终状态是否前一状态
						  	String latestStatus = resultList.get(0).getString("latest_state");
						  	if(!preStatus.equals(latestStatus)){
						  		//如何不是,则不更新状态
						  		ret.complete(boId);
						  		return;
						  	}	
					    }
					  
				    	JsonObject update = new JsonObject();
				    	update.put("$set", new JsonObject().put("latest_state", bizStatus).put("ts", getNowDate()));
						mongoClient.updateCollection(getCDOLatestTableName(bizObjectType, fromAcct, toAcct), query, update, result -> {
							  //String replyMsg = "ok";
							  if (result.succeeded()) {
								  ret.complete(boId);
							  } else {
					    		  Throwable err = result.cause();
					    		  String replyMsg = err.getMessage();
					    		  appActivity.getLogger().error(replyMsg, err);
					    		  ret.fail(err);
							  }
							});
				  } else {
					    //记录最后状态事实对象
				    	JsonObject insert = new JsonObject();
				    	insert.put("latest_state", bizStatus);
				    	insert.put("from_account", fromAcct);
				    	insert.put("to_account", toAcct);
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
/*				    	if(factData.containsKey("biz_thread")){
				    		insert.put("biz_thread", factData.getJsonObject("biz_thread").copy());				    		
				    	}*/
				    	
				    	insert.put("ts", getNowDate());
				    	mongoClient.insert(getCDOLatestTableName(bizObjectType, fromAcct, toAcct), insert, result -> {
						  //String replyMsg = "ok";
						  if (result.succeeded()) {
							  ret.complete(boId);
						  } else {
				    		  Throwable err = result.cause();
				    		  String replyMsg = err.getMessage();
				    		  appActivity.getLogger().error(replyMsg, err);
				    		  ret.fail(err);
						  }
						});
				    	
/*				    	Future<String> stubFuture = Future.future();
				    	stubFuture.setHandler(stubRet->{
				    		
				    	});
				    	
				    	String partnerAcct = "";
				    	if(roleDirection == BizRoleDirection.FROM){
				    		partnerAcct = toAcct;
				    	}else{
				    		partnerAcct = fromAcct;
				    	}
				    	//记录CDO对象存根信息
				    	recordStubForCDO(factData, boId, partnerAcct, stubFuture);*/
				    	
				  }
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  appActivity.getLogger().error(replyMsg, err);
	    		  ret.fail(err);
			  }		  
			});
	}
	
	public JsonObject buildStubForCDO(JsonObject factData, String boId, String partnerAcct) {
	    //记录最后状态事实对象
    	JsonObject insert = new JsonObject();

    	insert.put("partner", partnerAcct);
    	insert.put("bo_id", boId);
    	
    	return insert;
	}
	
/*	private void recordStubForCDO(JsonObject factData, String boId, String partnerAcct, Future<String> ret){
	    //记录最后状态事实对象
    	JsonObject insert = new JsonObject();

    	insert.put("partner", partnerAcct);
    	insert.put("account", appActivity.getAppInstContext().getAccount());
    	insert.put("bo_id", boId);
    	insert.put("ts", getNowDate());
    	
    	JsonObject subObject = buildStubForCDO(factData, boId, partnerAcct);
    	if(subObject != null){
    		insert.put("stub", subObject);
    	}
    	
    	
    	
    	MongoClient mongoCli = getCurrentDataSource().getMongoClient();  
    	
    	mongoCli.insert(getDBTableName(this.appActivity.getBizObjectType()), insert, result -> {
		  //String replyMsg = "ok";
		  if (result.succeeded()) {
			  ret.complete(boId);
		  } else {
    		  Throwable err = result.cause();
    		  String replyMsg = err.getMessage();
    		  appActivity.getLogger().error(replyMsg, err);
    		  ret.fail(err);
		  }
		});
    	
	}*/
	
	
	
/*	@Override
	public void recordFactDatas(String bizObjectType, List<JsonObject> factDatas, JsonObject actor, Handler<AsyncResult<List<String>>> next){
		ActionDescriptor actionDesc = getActionDesc();
		BizStateSwitchDesc stateSwitchDesc = actionDesc.getBizStateSwitch();
		String preState = stateSwitchDesc.getFromState();
		String newState = stateSwitchDesc.getToState();
		
		recordFactDatas(bizObjectType, factDatas, preState, newState, actor, next);
	}*/


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
	
    /**
     * 按指定bo_id查询最终状态数据
     */
	@Override
	public void queryLatestCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, String boId, JsonObject fields, Handler<AsyncResult<JsonObject>> next){
		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		
		String fromAcctTemp;
		String toAcctTemp;
		if(roleDirection == BizRoleDirection.FROM){
			fromAcctTemp = this.appActivity.getAppInstContext().getAccount();
			toAcctTemp = partnerAcct;
		}else {			
			fromAcctTemp = partnerAcct;
			toAcctTemp = this.appActivity.getAppInstContext().getAccount();
		}
		final String fromAcct = fromAcctTemp;
		final String toAcct = toAcctTemp;
		
		//String account = this.appActivity.getAppInstContext().getAccount();
		query = appActivity.getCDODatasource().getDataPersistentPolicy().getQueryConditionForMongo(fromAcct, toAcct, query);
   	
		MongoClient mongoClient = this.appActivity.getCDODatasource().getMongoClient();

		//MongoClient mongoClient = getCurrentDataSource().getMongoClient();     
		
		if(fields == null){
			mongoClient.find(getCDOLatestTableName(bizObjectType, fromAcct, toAcct), query, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  if (resultList != null && resultList.size() > 0) {
						  String latestState = resultList.get(0).getString("latest_state");
						  queryCDO(roleDirection, partnerAcct, fromAcct, toAcct, bizObjectType, boId, latestState, fields, mongoClient, next);
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

			mongoClient.findWithOptions(getCDOLatestTableName(bizObjectType, fromAcct, toAcct), query, findOptions, res -> {
				  if (res.succeeded()) {
					  List<JsonObject> resultList = res.result();
					  if (resultList != null && resultList.size() > 0) {
						  String latestState = resultList.get(0).getString("latest_state");
						  queryCDO(roleDirection, partnerAcct, fromAcct, toAcct, bizObjectType, boId, latestState, fields, mongoClient, next);
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
	
	 /**
     * 按指定状态查询单个BO
     */	
	private void queryCDO(BizRoleDirection roleDirection, String partnerAcct, String fromAcct, String toAcct, String bizObjectType, String boId, String boStatus, JsonObject fields, MongoClient mongoClient, Handler<AsyncResult<JsonObject>> next){

		if(boStatus == null || boStatus.isEmpty()){
			queryLatestCDO(roleDirection, partnerAcct, bizObjectType, boId, fields, next);
			return;
		}
		
		
		JsonObject query = new JsonObject();
		query.put("bo_id", boId);
		
		query = this.appActivity.getCDODatasource().getDataPersistentPolicy().getQueryConditionForMongo(fromAcct, toAcct, query);
		
		Future<JsonObject> ret = Future.future();
		ret.setHandler(next);
		
		if(fields == null){
			mongoClient.find(getCDOTableName(bizObjectType, boStatus, fromAcct, toAcct), query, res -> {
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

			mongoClient.findWithOptions(getCDOTableName(bizObjectType, boStatus, fromAcct, toAcct), query, findOptions, res -> {
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

    /**
     * 按指定状态查询单个BO
     */
	@Override
	public void queryCDO(BizRoleDirection roleDirection, String partnerAcct, String bizObjectType, String boId, String boStatus, JsonObject fields, Handler<AsyncResult<JsonObject>> next){
		
		String fromAcctTemp;
		String toAcctTemp;
		if(roleDirection == BizRoleDirection.FROM){
			fromAcctTemp = this.appActivity.getAppInstContext().getAccount();
			toAcctTemp = partnerAcct;
		}else {			
			fromAcctTemp = partnerAcct;
			toAcctTemp = this.appActivity.getAppInstContext().getAccount();
		}
		final String fromAcct = fromAcctTemp;
		final String toAcct = toAcctTemp;
		
		MongoClient mongoClient = appActivity.getCDODatasource().getMongoClient();
		
		queryCDO(roleDirection, partnerAcct, fromAcct, toAcct, bizObjectType, boId, boStatus, fields, mongoClient, next);
		
	}
	
    public String getCDOTableName(String boType, String boStatus, String fromAccount, String toAccount){		
		String tableName = boType + "_" + boStatus;		
		return this.appActivity.getCDODatasource().getDBTableName(fromAccount, toAccount, tableName);
	}
	
	public String getCDOLatestTableName(String boType, String fromAccount, String toAccount){
		String boLatestTb = boType + "_" + MONGO_BO_LATEST_STATE;		
		return this.appActivity.getCDODatasource().getDBTableName(fromAccount, toAccount, boLatestTb);
	}	
	
	
}