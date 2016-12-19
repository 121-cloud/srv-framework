/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;

import java.util.ArrayList;
import java.util.List;

import otocloud.framework.app.common.AppConfiguration;
import otocloud.framework.app.common.AppInstanceContext;
import otocloud.framework.app.engine.AppService;
import otocloud.framework.app.persistence.DataPersistentPolicy;
import otocloud.framework.app.persistence.DataPersistentPolicyFactory;
import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.app.persistence.OtoCloudCDODataSource;
import otocloud.framework.core.OtoCloudEventDescriptor;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponentImpl;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月21日
 * @author lijing@yonyou.com
 */
public abstract class AppActivityImpl extends OtoCloudComponentImpl implements AppActivity {

	protected OtoCloudAppDataSource otoCloudAppDataSource;
	protected ActivityDescriptor activityDescriptor;	
	//活动的完成事件:{appPrefix}.platform.activity.completed
	public static final String ACTIVITY_COMPLETED_BASE = "platform.activity.completed";	
	
	
	@Override 
    public void start(Future<Void> startFuture) throws Exception {
		//所依赖的服务
		dependencies = config().getJsonObject("dependencies", null);
		
		configLogging();	
		comInstTag = getAppInstContext().getAccount();
    	createAppMongoClient();
		eventHandlers = registerEventHandlers();
		if(eventHandlers != null && eventHandlers.size() > 0){
			List<HandlerDescriptor> restAPIs = new ArrayList<HandlerDescriptor>();
			eventHandlers.forEach(value -> {
				value.register(getEventBus());
				ActionHandlerRegistry actionHandler = (ActionHandlerRegistry)value;
				HandlerDescriptor handlerDesc= actionHandler.getActionDesc().getHandlerDescriptor();
				if(handlerDesc.getRestApiURI() != null){
					restAPIs.add(handlerDesc);
				}
			});
			if(restAPIs.size() > 0){
				service.registerRestAPIs(getName(), restAPIs, startFuture);
			}else{
				startFuture.complete();
			}
		}else{
			startFuture.complete();
		}

	}
	
	@Override 
    public void stop(Future<Void> stopFuture) throws Exception{			
		Future<Void> innerStopFuture = Future.future();
		super.stop(innerStopFuture);
		innerStopFuture.setHandler(stopRet->{
			if(otoCloudAppDataSource != null)
				otoCloudAppDataSource.close();
			if(stopRet.succeeded()){
				stopFuture.complete();
			}else{
				stopFuture.fail(stopRet.cause());
			}
		});
	}
	
	//获取应用上下文	
	@Override
	public AppInstanceContext getAppInstContext(){
		return ((AppService)service).getAppInstContext();
	}	
	
	@Override
	public AppService getAppService(){
		return (AppService)service;
	}
	
	@Override
	public String buildEventAddress(String addressBase){
		return getAppService().buildEventAddress( getName() + "." + addressBase );
	}
	
	@Override
	public String buildApiRegAddress(String addressBase){
		return getAppService().getAppEngine().buildEventAddress( getName() + "." + addressBase );
	}
	
	
    //创建MongoClient
    private void createAppMongoClient() {
    	JsonObject pscCfg = config();
        JsonObject mongoClientCfg = pscCfg.getJsonObject(AppConfiguration.APP_DATASOURCE, null);  
        if(mongoClientCfg != null){
        	this.logger.info("配置" + this.getName() + "的mongo连接：" + mongoClientCfg.toString());
        	String dataPersistentPolicy = mongoClientCfg.getString(AppConfiguration.DATASHARDING_POLICY, "");
        	
        	DataPersistentPolicy persistentPolicy = null;
        	if(this instanceof DataPersistentPolicy){
        		persistentPolicy = (DataPersistentPolicy)this;
        	}else{
        		DataPersistentPolicyFactory dataPersistentPolicyFactory = new DataPersistentPolicyFactory(dataPersistentPolicy);
        		persistentPolicy = dataPersistentPolicyFactory.getPolicy();
        	}        	
        	
	        otoCloudAppDataSource = new OtoCloudAppDataSource(vertx, mongoClientCfg, persistentPolicy);
			//otoCloudAppDataSource.init(vertx, mongoClientCfg);			
        }        
	}

    @Override
    public OtoCloudAppDataSource getAppDatasource(){
    	if(otoCloudAppDataSource != null){
    		this.logger.info("使用" + this.getName() + "配置的连接");
    		return otoCloudAppDataSource; 
    	}
    	return getAppService().getAppDatasource();
    }

    @Override
    public OtoCloudCDODataSource getCDODatasource(){
    	return getAppService().getCDODatasource();
    }
    
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActivityDescriptor getActivityDescriptor() {
		//后续从统一的元数据服务取，而不是从Activity对象中获取		
		
		
		//暂时实现为从Activity对象中获取描述符
		if(activityDescriptor == null){
			List<ActionDescriptor> actionsDescLst = new ArrayList<ActionDescriptor>();	
			
			List<OtoCloudEventDescriptor> stateSwitchBizEvents = new ArrayList<OtoCloudEventDescriptor>();
			
			List<ActionHandlerRegistry> actionHandlers = getActionHandlers();
			if(actionHandlers != null && actionHandlers.size() > 0){
				actionHandlers.forEach(actionHandler -> {					
					//添加供应商初始化WEB接口
					ActionDescriptor actionDesc = actionHandler.getActionDesc();
					if(actionDesc != null){
						actionsDescLst.add(actionDesc);
						
						BizStateSwitchDesc bizStateSwitchDesc = actionDesc.getBizStateSwitch();
						if(bizStateSwitchDesc != null && bizStateSwitchDesc.getWebExpose()){
							//地址
							String stateChangedEvent = BizStateSwitchDesc.buildStateSwitchEventAddress(this.getBizObjectType(),
									bizStateSwitchDesc.getFromState(), bizStateSwitchDesc.getToState());
							OtoCloudEventDescriptor eventDesc = new OtoCloudEventDescriptor(stateChangedEvent, null, false, null);
							
							stateSwitchBizEvents.add(eventDesc);
						}
					}
				});
			}
			
			List<BizRoleDescriptor> bizRolesDesc = exposeBizRolesDesc();
			List<OtoCloudEventDescriptor> bizEventsDesc = exposeOutboundBizEventsDesc();

			if(stateSwitchBizEvents.size() > 0){
				if(bizEventsDesc == null){
					bizEventsDesc = new ArrayList<OtoCloudEventDescriptor>();				
				}
				for (OtoCloudEventDescriptor stateSwitchBizEvent : stateSwitchBizEvents) {
					bizEventsDesc.add(stateSwitchBizEvent);		
				}
			}
			
			activityDescriptor = new ActivityDescriptor(getBizObjectType(),getName(),
					bizRolesDesc, actionsDescLst, bizEventsDesc);	
		}		
		return activityDescriptor;
	}

	
	/**
	 * {@inheritDoc}
	 */
/*	@Override
	public List<BizRole> getActivityRolesForCurrentAccount() {
		AppInstanceContext context = this.getAppInstContext();
		ActivityDescriptor activityDesc = getActivityDescriptor();
		List<BizRole> retBizRoles = new ArrayList<BizRole>();		
		List<BizRoleDescriptor> appBizRoles = activityDesc.getBizRolesDesc();
		if(appBizRoles != null && appBizRoles.size() > 0){
			appBizRoles.forEach(bizRoleDesc -> {
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
		return retBizRoles;
	}*/


	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ActionHandlerRegistry> getActionHandlers() {	
		List<ActionHandlerRegistry> actionHandlers = new ArrayList<ActionHandlerRegistry>();
		if(eventHandlers != null && eventHandlers.size() > 0){
			eventHandlers.forEach(value -> {
				actionHandlers.add((ActionHandlerRegistry)value);
			});
		}		
		return actionHandlers;
	}

	public String buildActivityCompletedEventAddress(){
		return getActivityDescriptor().getActivityName() + "." + ACTIVITY_COMPLETED_BASE;
	}
	
	private JsonObject createCompletedEventMsg(String bizObjId, JsonObject actor) {
		  JsonObject msg = new JsonObject();
		  msg.put("account", getAppInstContext().getAccount());
		  msg.put("bizid", bizObjId);
		  msg.put("actor", actor);	  
		  
		  return msg;
		}
	
	public void setCompleted(String bizObjId, JsonObject actor){	
		String address = buildActivityCompletedEventAddress();
		
		JsonObject msgObj = createCompletedEventMsg(bizObjId, actor);
		
		//发布状态变化事件
		this.getEventBus().publish(address, msgObj);		
		
		//记录日志
		getLogger().info(actor, "业务:" + bizObjId + "的业务活动:" + getActivityDescriptor().getActivityName() + "已经完成");		
		
	}
	
	//开始根业务
/*	{
	    "_id" : ObjectId("55f683c7c9bb0dffc13f79bf"),
	    "root_bo_type" : "po",
	    "root_bo_id" : "PO001",
	    "root_status" : 1
	}*/
	public void bizRootStart(String rootObjectType, String rootObjectId, Handler<AsyncResult<Void>> next){
    	MongoClient mongoClient = getAppDatasource().getMongoClient();	  	
 		
		Future<Void> ret = Future.future();
		ret.setHandler(next);
		
 		String account = this.getAppInstContext().getAccount(); 
    		
		JsonObject bizRootData = new JsonObject();
		bizRootData.put("root_bo_type", rootObjectType);
		bizRootData.put("root_bo_id", rootObjectId);
		bizRootData.put("root_status", 1);
		bizRootData.put("account", account);
	    mongoClient.insert(getBizRootTableName(), bizRootData, res -> {
			  if (res.succeeded()) {
				  ret.complete();
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  getLogger().error(replyMsg, err);    	    
	    		  ret.fail(err);
			  }	
			});
 
	}
	
	//结束根业务
	public void bizRootEnd(String rootObjectType, String rootObjectId, Handler<AsyncResult<Void>> next){
    	MongoClient mongoClient = getAppDatasource().getMongoClient();  	  	
 		
		Future<Void> ret = Future.future();
		ret.setHandler(next);
		
		String account = this.getAppInstContext().getAccount(); 
    		
		JsonObject query = new JsonObject();
		query.put("root_bo_type", rootObjectType);
		query.put("root_bo_id", rootObjectId);
		query.put("account", account);
		
		JsonObject updateData = new JsonObject();
		updateData.put("root_status", 2);		
		
	    mongoClient.updateCollection(getBizRootTableName(), query, updateData, res -> {
			  if (res.succeeded()) {
				  ret.complete();
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  getLogger().error(replyMsg, err);    	    
	    		  ret.fail(err);
			  }	
			});
		
	}
	
	//记录业务活动开始
	public void bizActivityStart(String rootObjectType, String rootObjectId, JsonArray previousActivities,
			String currentBoId,	Handler<AsyncResult<Void>> next){
		
    	MongoClient mongoClient = getAppDatasource().getMongoClient(); 	  	
 		
		Future<Void> ret = Future.future();
		ret.setHandler(next);
		
 		String account = this.getAppInstContext().getAccount(); 
    		
		JsonObject bizActivityData = new JsonObject();
		bizActivityData.put("root_bo_type", rootObjectType);
		bizActivityData.put("root_bo_id", rootObjectId);
		bizActivityData.put("account", account);
		
		if(previousActivities != null && previousActivities.size() > 0){
			bizActivityData.put("previous_activities", previousActivities);
		}
		
		JsonObject currentActivity = new JsonObject();
		currentActivity.put("activity", this.getName());
		bizActivityData.put("bo_type", this.getBizObjectType());
		bizActivityData.put("bo_id", currentBoId);
		
		bizActivityData.put("current_activity", currentActivity);
		bizActivityData.put("current_activity_status", 1);
		
		
	    mongoClient.insert(getBizThreadTableName(), bizActivityData, res -> {
			  if (res.succeeded()) {
				  ret.complete();
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  getLogger().error(replyMsg, err);    	    
	    		  ret.fail(err);
			  }	
			});
 
	}	
	
	
	//记录业务活动完成
	public void bizActivityEnd(String rootObjectType, String rootObjectId, String currentBoId, Handler<AsyncResult<Void>> next){
		
    	MongoClient mongoClient = getAppDatasource().getMongoClient();  	
 		
		Future<Void> ret = Future.future();
		ret.setHandler(next);
		
 		String account = this.getAppInstContext().getAccount(); 
    		
		JsonObject query = new JsonObject();
		query.put("root_bo_type", rootObjectType);
		query.put("root_bo_id", rootObjectId);
		query.put("current_activity.activity", this.getName());
		query.put("current_activity.bo_id", currentBoId);
		query.put("account", account);
		
		JsonObject updateData = new JsonObject();
		updateData.put("current_activity_status", 2);
		
	    mongoClient.updateCollection(getBizThreadTableName(), query, updateData, res -> {
			  if (res.succeeded()) {
				  ret.complete();
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  getLogger().error(replyMsg, err);    	    
	    		  ret.fail(err);
			  }	
			});
	    
	}
	
	//查询正在进行的业务活动
	public void queryHotBizActivities(String rootObjectType, String rootObjectId, Handler<AsyncResult<List<ActivityThread>>> result){
    	MongoClient mongoClient = getAppDatasource().getMongoClient();	  	
 		
		Future<List<ActivityThread>> ret = Future.future();
		ret.setHandler(result);
		
 		//String account = this.getAppInstContext().getAccount(); 
    		
		JsonObject query = new JsonObject();
		query.put("root_bo_type", rootObjectType);
		query.put("root_bo_id", rootObjectId);
		query.put("current_activity_status", 1);
		
	    mongoClient.find(getBizThreadTableName(), query, res -> {
			  if (res.succeeded()) {				  
				  List<JsonObject> retData = res.result();
				  List<ActivityThread> retActs = new ArrayList<ActivityThread>();
				  if(retData != null && retData.size() > 0){
					  retData.forEach(item->{
						  ActivityThread act = new ActivityThread();
						  act.fromJsonObject(item);
						  retActs.add(act);
					  });					  
				  }				  
				  ret.complete(retActs);
			  } else {
	    		  Throwable err = res.cause();
	    		  String replyMsg = err.getMessage();
	    		  getLogger().error(replyMsg, err);    	    
	    		  ret.fail(err);
			  }	
			});		
		
	}
	
	//向下跟踪子业务线索
    public void trackBizThreadForBoId(String boId, Handler<AsyncResult<List<JsonObject>>> result){
    
   		MongoClient mongoClient = getAppDatasource().getMongoClient();  	
	
		Future<List<JsonObject>> ret = Future.future();
		ret.setHandler(result);
		
 		String account = this.getAppInstContext().getAccount(); 
		
		JsonObject query = new JsonObject();
  		query.put("previous_activities.activity", this.getName());
 		query.put("previous_activities.bo_type", this.getBizObjectType());
		query.put("previous_activities.bo_id", boId);
		query.put("account", account);
		
		FindOptions options = new FindOptions();
		options.setFields(new JsonObject().put("current_activity.activity", 1)
				.put("current_activity.bo_type", 1).put("current_activity.bo_id", 1));    		
		
	    mongoClient.findWithOptions(getBizThreadTableName(), query, options, res -> {
		  if (res.succeeded()) {				  
			  List<JsonObject> nextActivities = res.result(); 
			  ret.complete(nextActivities);
		  } else {
    		  Throwable err = res.cause();
    		  String replyMsg = err.getMessage();
    		  getLogger().error(replyMsg, err);    	    
    		  ret.fail(err);
		  }	
		});	
    	
    	
    }
	
	
	public String getDBTableName(String tableName){
		String account = getAppInstContext().getAccount();	
		return getAppDatasource().getDBTableName(account, tableName);
	}	
	
	private String getBizRootTableName(){
		String account = this.getAppInstContext().getAccount();	
		return this.getAppDatasource().getBizRootTableName(account);
	}
	
	private String getBizThreadTableName(){
		String account = this.getAppInstContext().getAccount();	
		return this.getAppDatasource().getBizThreadTableName(account);
	}
}
