/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import otocloud.common.OtoCloudDirectoryHelper;
import otocloud.common.OtoConfiguration;
import otocloud.common.util.JsonUtil;
import otocloud.framework.app.common.AppConfiguration;
import otocloud.framework.app.common.AppInstanceContext;
import otocloud.framework.app.function.ActivityDescriptor;
import otocloud.framework.app.persistence.CDODataPersistentPolicy;
import otocloud.framework.app.persistence.CDODataPersistentPolicyFactory;
import otocloud.framework.app.persistence.DataPersistentPolicy;
import otocloud.framework.app.persistence.DataPersistentPolicyFactory;
import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.app.persistence.OtoCloudCDODataSource;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;
import otocloud.framework.core.OtoCloudServiceForVerticleImpl;
import otocloud.framework.core.VertxInstancePool;
import otocloud.framework.core.factory.OtoCloudServiceFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;




/**
 * TODO: DOCUMENT ME!
 * 
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public abstract class AppServiceEngineImpl extends OtoCloudServiceForVerticleImpl implements AppServiceEngine {

	public static final String GET_SUBSCRIBERS = "platform.appsubscribers.get";// 获取应用的订阅账户

	protected String appDesc;
	protected String masterRole;
	protected JsonArray appInstScope;
	protected int distributedNodeIndex = 0;
	
    protected OtoCloudAppDataSource otoCloudAppDataSource;
    protected OtoCloudCDODataSource otoCloudCDODataSource;


	protected Map<String, AppService> appInstances;
	//protected Map<String, String> accountAppInsts; 
	
	protected boolean isWebServerHost;
	protected AppWebServerImpl webServer;
	protected boolean isolationVertxPool;

	protected VertxInstancePool appVertxInstPool;
	
	
	public AppServiceEngineImpl(){
		super();
	}
	

	@Override
	public void afterInit(Future<Void> initFuture) {		
		// 使用线程安全的List<T>
		Map<String, AppService> appSrvSet = new HashMap<String, AppService>();
		appInstances = Collections.synchronizedMap(appSrvSet);
		//Map<String, String> accAppInstSet = new HashMap<String, String>();
		//accountAppInsts = Collections.synchronizedMap(accAppInstSet);	

		if(isWebServerHost()){
			webServer = (AppWebServerImpl)createWebServer();
			if(webServer != null){	
				Vertx  webSrvHostVertx = vertx;
				if(webSrvHostVertx == null){
					webSrvHostVertx = this.vertxInstance;
				}
				webServer.init(getRealServiceName(), null, webSrvHostVertx, srvCfg.getJsonObject(OtoConfiguration.WEBSERVER_CFG), null);
			}
		}	
		
		int appVertxPoolSize = AppConfiguration.APP_VERTX_NUMBER;
		if(srvCfg.containsKey(AppConfiguration.APP_VERTX_NUMBER_KEY)){
			appVertxPoolSize = srvCfg.getInteger(AppConfiguration.APP_VERTX_NUMBER_KEY);
		}				
		
		if(isolationVertxPool){		
			appVertxInstPool = new VertxInstancePool();
			
	    	Future<Void> vertxInstFuture = Future.future();
	    	appVertxInstPool.init(srvCfg, clusterCfg, appVertxPoolSize, vertxOptionsCfg, vertxInstFuture);
	    	vertxInstFuture.setHandler(run -> {
	    		if (run.succeeded()) {
	    			
	    			futureStatusComplete();
					initFuture.complete();        				
					logger.info(appDesc + "引擎初始化成功");
	
	    		}else{
	    			
	    			futureStatusRollback();
	               	Throwable err = run.cause();               	
	               	initFuture.fail(err);
	            	logger.error(err.getMessage(), err);
	    		}
	    	}); 
		}else{
			if(container == null){
				appVertxInstPool = new VertxInstancePool();
		    	Future<Void> vertxInstFuture = Future.future();
		    	appVertxInstPool.init(this.vertxInstance, vertxInstFuture);
		    	vertxInstFuture.setHandler(run -> {
		    		if (run.succeeded()) {
		    			
		    			futureStatusComplete();
						initFuture.complete();        				
						logger.info(appDesc + "引擎初始化成功");
		
		    		}else{
		    			
		    			futureStatusRollback();
		               	Throwable err = run.cause();               	
		               	initFuture.fail(err);
		            	logger.error(err.getMessage(), err);
		    		}
		    	}); 
			}else{
				appVertxInstPool = container.getVertxInstancePool();
				futureStatusComplete();
				initFuture.complete();     
				logger.info(appDesc + "引擎初始化成功");
			}
			
		}


	}
	
	@Override
	public WebServer createWebServer() {
		return new AppWebServerImpl();
	}
	
	@Override
	public void configService(){
		super.configService();

		isolationVertxPool = srvCfg.getBoolean("isolation_vertx_pool", true);
		
		if(srvCfg.containsKey(AppConfiguration.MASTER_ROLE)){
			masterRole = srvCfg.getString(AppConfiguration.MASTER_ROLE);	
		}
		appDesc = srvCfg.getString(AppConfiguration.APP_DESC_KEY);	
		if(srvCfg.containsKey(AppConfiguration.WEBSERVER_HOST)){
			isWebServerHost = srvCfg.getBoolean(AppConfiguration.WEBSERVER_HOST);
		}else {
			isWebServerHost = false;
		}
		if(srvCfg.containsKey(AppConfiguration.APP_INST_SCOPE))
			appInstScope = srvCfg.getJsonArray(AppConfiguration.APP_INST_SCOPE);
		if(srvCfg.containsKey("distributed_node_index"))
			distributedNodeIndex = srvCfg.getInteger("distributed_node_index");
		
		//createAppMongoClient();
	}
	
    //创建MongoClient
    private void createAppMongoClient() {  
    	synchronized(this){ 
    		if(otoCloudAppDataSource == null){
		        JsonObject mongoClientCfg = srvCfg.getJsonObject(AppConfiguration.APP_DATASOURCE, null);  
		        if(mongoClientCfg != null){
		        	String dataPersistentPolicy = mongoClientCfg.getString(AppConfiguration.DATASHARDING_POLICY, "");
		        	DataPersistentPolicy persistentPolicy = null;
		        	if(this instanceof DataPersistentPolicy){
		        		persistentPolicy = (DataPersistentPolicy)this;
		        	}else{
		        		DataPersistentPolicyFactory dataPersistentPolicyFactory = new DataPersistentPolicyFactory(dataPersistentPolicy);
		        		persistentPolicy = dataPersistentPolicyFactory.getPolicy();
		        	}
			        otoCloudAppDataSource = new OtoCloudAppDataSource(vertxInstance, mongoClientCfg, persistentPolicy);
					//otoCloudAppDataSource.init(vertxInstance, mongoClientCfg);			
		        }      
    		}
    	}
	}
    
    //创建MongoClient
    private void createCDOMongoClient() { 
    	synchronized(this){ 
    		if(otoCloudCDODataSource == null){
		        JsonObject mongoClientCfg = srvCfg.getJsonObject(AppConfiguration.CDO_DATASOURCE, null);  
		        if(mongoClientCfg != null){
		        	String dataPersistentPolicy = mongoClientCfg.getString(AppConfiguration.DATASHARDING_POLICY, "");		        	
		        	
		        	CDODataPersistentPolicy persistentPolicy = null;
		        	if(this instanceof CDODataPersistentPolicy){
		        		persistentPolicy = (CDODataPersistentPolicy)this;
		        	}else{
		        		CDODataPersistentPolicyFactory dataPersistentPolicyFactory = new CDODataPersistentPolicyFactory(dataPersistentPolicy);
		        		persistentPolicy = dataPersistentPolicyFactory.getPolicy();
		        	}

			        otoCloudCDODataSource = new OtoCloudCDODataSource(vertxInstance, mongoClientCfg, persistentPolicy);
					//otoCloudAppDataSource.init(vertxInstance, mongoClientCfg);			
		        }  
    		}
        }   
	}
    
    @Override
    public OtoCloudAppDataSource getAppDatasource(){
    	if(otoCloudAppDataSource == null)
    		createAppMongoClient();
    	return otoCloudAppDataSource;
    }
    
    @Override
    public OtoCloudCDODataSource getCDODatasource(){
    	if(otoCloudCDODataSource == null)
    		createCDOMongoClient();
    	return otoCloudCDODataSource;
    }

	
/*	@Override
	public List<OtoCloudComponent> createServiceComponents(){
		return null;
	}*/
	

	@Override
	public List<OtoCloudEventHandlerRegistry> createHandlers(){ 
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		OtoCloudEventHandlerRegistry appInstLoadHandler = new AppInstLoadHandler(this);
		ret.add(appInstLoadHandler);
		OtoCloudEventHandlerRegistry appStatusCtrlHandler = new AppStatusCtrlHandler(this);
		ret.add(appStatusCtrlHandler);
		OtoCloudEventHandlerRegistry appStatusQueryHandler = new AppStatusQueryHandler(this);
		ret.add(appStatusQueryHandler);
		OtoCloudEventHandlerRegistry activityDeploymentHandler = new AppActivityDeploymentHandler(this);
		ret.add(activityDeploymentHandler);
		OtoCloudEventHandlerRegistry activityUndeploymentHandler = new AppActivityUndeploymentHandler(this);
		ret.add(activityUndeploymentHandler);
		
		apiURIResolver = new AppRestApiURIResolver(this);
		ret.add(apiURIResolver);
		
		return ret;
	}
	
	
	@Override
	public void afterRun(Future<Void> runFuture){
			
		AppInstRunFuture innerRunFuture = new AppInstRunFuture();			
		innerRunFuture.RunFuture = Future.future();
		// 创建应用实例
		createAppInstances(innerRunFuture);	
		innerRunFuture.RunFuture.setHandler(ret -> {
    		if(ret.succeeded()){   
    			//运行web服务器
    			runWebServer();
    			
    			futureStatusComplete();
    			runFuture.complete();
    		}else{		
    			futureStatusRollback();
    			runFuture.fail(ret.cause());
    		}  		
			
		});

	}
	
	@Override
	public boolean isWebServerHost(){
		return isWebServerHost;
	}
	
	@Override
	public void runWebServer(){
		if(isWebServerHost() && webServer != null){				
			Iterator<AppService> iterator = appInstances.values().iterator();		
			if(iterator.hasNext()){
				AppService oneServer = iterator.next();
				List<ActivityDescriptor> activityDescList = oneServer.getActivityDescriptors();
				webServer.restRoute(activityDescList);
			}
			List<AppService> nonWebHostAppInsts = appInstances.values().stream().filter(appInst -> appInst.isWebServerHost() == false).collect(Collectors.toList());
			webServer.busRoute(nonWebHostAppInsts);
			webServer.listen();
		}
	}	

	@Override
	public void afterStop(Future<Void> stopFuture) {
		Integer size = appInstances.size();
		if(size == 0){
			futureStatusComplete();
			stopFuture.complete();
		}
		
		if(isWebServerHost() && webServer != null){
			webServer.close();
		}
		
		Future<Void> closeAppSrvFuture = Future.future();
		closeAppServices(closeAppSrvFuture);
		closeAppSrvFuture.setHandler(stop -> {
			if(otoCloudAppDataSource != null)
				otoCloudAppDataSource.close();
    		if(stop.failed()){
    			futureStatusRollback();
               	Throwable err = stop.cause();
            	logger.error(err.getMessage(), err);
            	stopFuture.fail(err);
    		}else{
    			clean();
    			futureStatusComplete();
				stopFuture.complete();
    		}    		
		}); 
	}
	
	private void clean(){
		appInstances.clear();
		//accountAppInsts.clear();
	}
	
	private void closeAppServices(Future<Void> stopFuture){
		Integer size = appInstances.size();
		AtomicInteger stoppedCount = new AtomicInteger(0);		
		appInstances.forEach((key,appInst) -> {
			try{
				Future<Void> appStopFuture = Future.future();
				appInst.close(appStopFuture);
				appStopFuture.setHandler(stop -> {
            		if(stop.failed()){
                       	Throwable err = stop.cause();
                    	logger.error(err.getMessage(), err);
            		}
            		stopAppCompletedHandle(stopFuture, stoppedCount, size);
            	}); 					
			}catch(Throwable t){
				stopAppCompletedHandle(stopFuture, stoppedCount, size);
				logger.error(t.getMessage(), t);
			}				
		});	
	}
	
	private void stopAppCompletedHandle(Future<Void> stopFuture, AtomicInteger stoppedCount, Integer appCount) {
		if (stoppedCount.incrementAndGet() == appCount) {
			//appInstances.clear();
			//accountAppInsts.clear();
			
			if(isolationVertxPool && appVertxInstPool != null){
				
				Future<Void> closePoolFuture = Future.future();
				appVertxInstPool.close(closePoolFuture);
				closePoolFuture.setHandler(stop -> {
            		if(stop.failed()){
            			//futureStatusRollback();
                       	Throwable err = stop.cause();
                    	logger.error(err.getMessage(), err);
                    	stopFuture.fail(err);
            		}else{
            			//futureStatusComplete();
        				stopFuture.complete();
            		}
				});        				
				
			}else{		
				//futureStatusComplete();
				stopFuture.complete();
			}        			

        }
	}
	

	// 加载应用实例
	private void createAppInstances(AppInstRunFuture runFuture) {
		
		//JsonArray appSubscribers = srvCfg.getJsonArray("app_subscribers", new JsonArray());		
		
		getAppSubscribers(srvCfg.getJsonArray(AppConfiguration.APP_INST_SCOPE, null), done->{
			  if (done.succeeded()) {				  
				  List<JsonObject> result = done.result();				  
				  if(result != null && result.size() > 0){					    
					    					  
						AtomicInteger runningCount = new AtomicInteger(0);
						Integer size = result.size();
						logger.debug("订阅账户数:" + size.toString());
						result.forEach(appSubscriber -> runAppInstance((JsonObject) appSubscriber, runFuture, runningCount, size));

				  }				  
			  }else{
  	    		  Throwable err = done.cause();
  	    		  String replyMsg = err.getMessage();
  	    		  getLogger().error(replyMsg, err);
  	    		  
	  			  runFuture.RunFuture.complete();				
	  			  logger.info("无账户订购此应用.");
	  			  
			  }
		});


	}
	
	
	private void closeDBConnect(SQLConnection conn){
		conn.close(handler->{
			if (handler.failed()) {
				Throwable conErr = handler.cause();
				getLogger().error(conErr.getMessage(), conErr);
			} else {								
			}
		});				
	}
	
	private static String buildAppInstScopeSQLCondition(JsonArray params) {
		if (params == null || params.size() == 0)
			return "";
		List<String> singleConds = new ArrayList<String>();
		List<String> scopeConds = new ArrayList<String>();
		for (int i = 0; i < params.size(); i++) {
			String instScope = params.getString(i);
			String[] segStrings = ((String) instScope).split("~");
			if (segStrings.length == 2) {
				scopeConds.add(String.format("(id>=%s and id<=%s)",
						segStrings[0], segStrings[1]));
			} else {
				singleConds.add(segStrings[0]);
			}
		}

		StringBuilder paramStr = new StringBuilder("(");
		boolean hasCond = false;
		if (scopeConds.size() > 0) {
			hasCond = true;
			for (int i = 0; i < scopeConds.size(); i++) {
				if (i > 0)
					paramStr.append(" or ");
				paramStr.append(scopeConds.get(i));
			}
		}
		if (singleConds.size() > 0) {
			StringBuilder inCondStr = new StringBuilder("id in(");
			for (int i = 0; i < singleConds.size(); i++) {
				if (i > 0)
					inCondStr.append(",");
				inCondStr.append(singleConds.get(i));
			}
			inCondStr.append(")");

			if (hasCond) {
				paramStr.append(" or ").append(inCondStr);
			} else {
				paramStr.append(inCondStr);
			}
		}

		paramStr.append(")");
		return paramStr.toString();
	}
	
	private void getAppSubscribers(JsonArray appInstScope, Handler<AsyncResult<List<JsonObject>>> done){
			
		  Future<List<JsonObject>> retFuture = Future.future();
		  retFuture.setHandler(done);
		  
		  String condition = buildAppInstScopeSQLCondition(appInstScope);
		  
		  String querySql;
		  
		  if(condition == null || condition.isEmpty()){
			  querySql = "SELECT org_acct_id,biz_role_id FROM acct_app WHERE code=? AND d_app_id=? AND app_version_id=?";
		  }else{
			  querySql = "SELECT org_acct_id,biz_role_id FROM acct_app WHERE code=? AND d_app_id=? AND app_version_id=? and " + condition;
		  }
		  
		  try{
		  
			  JDBCClient sqlClient = this.getSysDatasource().getSqlClient();
			  
			  sqlClient.getConnection(connRes -> {
					if (connRes.succeeded()) {
						final SQLConnection conn = connRes.result();				
						conn.setAutoCommit(true, res ->{
						  if (res.failed()) {
							  closeDBConnect(conn);
			  	    		  Throwable err = res.cause();
			  	    		  String replyMsg = err.getMessage();
			  	    		  getLogger().error(replyMsg, err);
			  	    		  retFuture.fail(err);
						  }else{										
							conn.queryWithParams(querySql, new JsonArray().add(this.getRealServiceName())
									.add(srvCfg.getInteger(AppConfiguration.APP_ID_KEY, -1))
									.add(srvCfg.getInteger(AppConfiguration.APP_VERSION_ID_KEY, -1)),
							  appSubRet->{								  
								  if (appSubRet.succeeded()) {
									  ResultSet result = appSubRet.result();
									  retFuture.complete(result.getRows());
								  }else{
					  	    		  Throwable err = appSubRet.cause();
					  	    		  String replyMsg = err.getMessage();
					  	    		  getLogger().error(replyMsg, err);
					  	    		  retFuture.fail(err);
								  }
								  closeDBConnect(conn);
							  });
						}
					});
	
				}else{
		    		  Throwable err = connRes.cause();
		    		  String replyMsg = err.getMessage();
		    		  getLogger().error(replyMsg, err);
		    		  retFuture.fail(err);
					}
				});
			  
		  }catch(Exception ex){
    		  String replyMsg = ex.getMessage();
    		  getLogger().error(replyMsg, ex);
    		  retFuture.fail(ex);
		  }		  

	}

	// 运行应用实例
	public void runAppInstance(JsonObject appSubscriber, AppInstRunFuture runFuture, AtomicInteger runningCount, Integer subscriberCount) {
		String account = JsonUtil.getJsonValue(appSubscriber,"org_acct_id");
		
		//如果服务以及存在，则返回
		if(appInstances.containsKey(account)){
			//String appInstId = accountAppInsts.get(account);
  			runFuture.AppInst = appInstances.get(account);
			runFuture.RunFuture.complete();				
			logger.info("账户[" + account + "]的应用实例正在运行.");			
			return;
		}
		
		AppInstanceContext appInstCtx = createAppInstContext(account, appSubscriber);

		AppService appInst = newAppInstance();
		appInst.setAppServiceEngine(this);
		
		Future<Void> initFuture = Future.future();
		
		//appInst.init(appInstCtx, getAppInstanceConfig(account), clusterCfg, initFuture);
		
		Vertx appVertx = appVertxInstPool.getVertx();		
		appInst.init(appInstCtx, getAppInstanceConfig(account), appVertx, this.clusterCfg, this.vertxOptionsCfg, initFuture);
		
		initFuture.setHandler(init -> {
            if (init.succeeded()) {   
            	try{
            	Future<Void> appRunFuture = Future.future();
            	appInst.run(appRunFuture);
            	appRunFuture.setHandler(run -> {
            		if (run.succeeded()) {
            			//String appInstId = appInstCtx.getInstId();
            			appInstances.put(appInstCtx.getAccount(), appInst);
            			//accountAppInsts.put(account, appInstId);
            			logger.info("账户[" + account + "]的应用实例启动成功!");		
            		}else{
                       	Throwable err = run.cause();
                    	logger.error(err.getMessage(), err);
            		}
            		if(runFuture != null)
            			runAppCompletedHandle(runFuture, appInst, runningCount, subscriberCount);
            	}); 
            	}catch(Throwable t){
            		if(runFuture != null)
            			runAppCompletedHandle(runFuture, appInst, runningCount, subscriberCount);
    				logger.error(t.getMessage(), t);            		
            	}              
            } else{
            	if(runFuture != null)
            		runAppCompletedHandle(runFuture, appInst, runningCount, subscriberCount);
            	Throwable err = init.cause();
            	logger.error(err.getMessage(), err);
            }
          });	

	}
	
	private void runAppCompletedHandle(AppInstRunFuture runFuture, AppService appInst, AtomicInteger runningCount, Integer subscriberCount) {
		if(runningCount != null && subscriberCount > 0){            		
       		if (runningCount.incrementAndGet() == subscriberCount) {	               			

       			runFuture.AppInst = appInst;
				runFuture.RunFuture.complete();	
				
				logger.info(appDesc + "引擎running");
            }
		}
	}
	
	private AppInstanceContext createAppInstContext(String account, JsonObject appSubscriber){	
/*		JsonArray frombizRoles = appSubscriber.getJsonArray("from_roles");
		JsonArray tobizRoles = appSubscriber.getJsonArray("to_roles");
*/		
/*		JsonArray fromRoles = new JsonArray();
		frombizRoles.forEach(role -> fromRoles.add(((JsonObject)role).getInteger("frombizrole").toString()));
		JsonArray toRoles = new JsonArray();
		tobizRoles.forEach(role -> toRoles.add(((JsonObject)role).getInteger("tobizrole").toString()));
*/		
/*		String instId = "";
		if(appSubscriber.containsKey("instid"))
			instId = JsonUtil.getJsonValue(appSubscriber,"instid");*/
		
		
		//String instName = appSubscriber.getString("instname");
		String bizRoleId = this.masterRole;
		if(appSubscriber.containsKey("biz_role_id"))
			bizRoleId = JsonUtil.getJsonValue(appSubscriber, "biz_role_id");
		
		String appId = getRealServiceName();
		
		AppInstanceContext appInstCtx = new AppInstanceContext(appId, appDesc, account, bizRoleId);
		
		return appInstCtx;
	}	


	// 获取应用实例配置
	private JsonObject getAppInstanceConfig(String account) {
		JsonObject appInstCfg = null;
		if(!srvCfg.containsKey(AppConfiguration.INST_CF_KEY)
				|| !srvCfg.getJsonObject(AppConfiguration.INST_CF_KEY).containsKey(account)){
			appInstCfg = new JsonObject();
		}else{
			appInstCfg = srvCfg.getJsonObject(AppConfiguration.INST_CF_KEY).getJsonObject(account);
		}
		
		//将公共配置注入应用实例配置
		reBuildAppInstCommonCfg(appInstCfg);
		return appInstCfg;
	}
	
	private void reBuildAppInstCommonCfg(JsonObject appInstCfg){
		
		if(srvCfg.containsKey(AppConfiguration.CLUSTER_CFG)){
			appInstCfg.put(AppConfiguration.CLUSTER_CFG,
					srvCfg.getJsonObject(AppConfiguration.CLUSTER_CFG).copy());
		}
		
		if(!srvCfg.containsKey(AppConfiguration.INST_COMMON))
			return;
		
		//将公共配置注入应用实例配置
		JsonObject instCommonCfg = srvCfg.getJsonObject(AppConfiguration.INST_COMMON);
		instCommonCfg.forEach(item -> {
			String keyString = item.getKey();
			//组件公共配置合并
			if(keyString == AppConfiguration.COMPONENT_COMMON){
				//实例配置可重写公共配置
				if(appInstCfg.containsKey(keyString)){				
					JsonObject componentCommon = (JsonObject)item.getValue();
					JsonObject instCompCommon = appInstCfg.getJsonObject(keyString);
					componentCommon.forEach(compCommonItem -> {
						String compCommkeyString = compCommonItem.getKey();
						if(!instCompCommon.containsKey(compCommkeyString)){
							Object valueObject =  compCommonItem.getValue();
							instCompCommon.put(compCommkeyString, valueObject);
						}
					});					
				}else{
					Object valueObject =  item.getValue();
					appInstCfg.put(keyString, ((JsonObject)valueObject).copy());	
				}
			}else if(keyString == AppConfiguration.COMPONENT_DEPLOY){//组件部署合并
				//实例配置可重写公共配置
				if(appInstCfg.containsKey(keyString)){				
					JsonArray componentDeployItems = (JsonArray)item.getValue();
					JsonArray instCompDeployItems = appInstCfg.getJsonArray(keyString);
					
					List<JsonObject> tempArray = new ArrayList<JsonObject>();
					
					for (Object componentDeployItem : componentDeployItems){
						JsonObject compDeployItemObj = (JsonObject)componentDeployItem;
						String compName = OtoCloudServiceFactory.getServiceName(compDeployItemObj.getString("name"));
						
						Boolean existItem = false;
						for (Object instCompDeployItem : instCompDeployItems) {
							JsonObject instCompDeployItemObj = (JsonObject)instCompDeployItem;
							String instCompName = OtoCloudServiceFactory.getServiceName(instCompDeployItemObj.getString("name"));
							if(instCompName.equals(compName)){
								existItem = true;
								break;
							}							
						}						
						if(!existItem){
							tempArray.add(compDeployItemObj);
						}
					}
					
					if(tempArray.size() > 0){
						tempArray.forEach(tempItem->{
							instCompDeployItems.add(tempItem.copy());
						});						
					}
					
				}else{
					Object valueObject =  item.getValue();
					appInstCfg.put(keyString, ((JsonArray)valueObject).copy());
				}

			}else{
				//实例配置可重写公共配置
				if(!appInstCfg.containsKey(keyString)){					
					Object valueObject =  item.getValue();
					if(valueObject instanceof JsonObject)
						appInstCfg.put(keyString, ((JsonObject)valueObject).copy());	
					else if(valueObject instanceof JsonArray)
						appInstCfg.put(keyString, ((JsonArray)valueObject).copy());	
					else
						appInstCfg.put(keyString, valueObject);
				}else{
					if(keyString == AppConfiguration.COMPONENT_CFG){
						JsonObject valueObject =  (JsonObject)item.getValue();
						JsonObject instValueObject = appInstCfg.getJsonObject(keyString);
						valueObject.forEach(itemObj->{
							String tempKey = itemObj.getKey();
							if(!instValueObject.containsKey(tempKey)){
								Object temValue =  itemObj.getValue();
								if(temValue instanceof JsonObject)
									instValueObject.put(tempKey, ((JsonObject)temValue).copy());	
								else if(temValue instanceof JsonArray)
									instValueObject.put(tempKey, ((JsonArray)temValue).copy());	
								else
									instValueObject.put(tempKey, temValue);
							}
						});

					}
					
					
				}
			}
		});
	}	

	
	@Override
	public Map<String, AppService> getAppServiceInstances(){
		return appInstances;
	}

	
	/**
	 * @return the webServer
	 */
	public WebServer getWebServer() {
		return webServer;
	}

    
	/**
	 * @return the masterRole
	 */
	public String getMasterRole() {
		return masterRole;
	}
	
	
	@Override
	public int getDistributedNodeIndex(){
		return distributedNodeIndex;
	}
	
	@Override
	public boolean checkInstanceScope(String acctId){
		if(appInstScope == null)
			return true;
		
		for(Object scope: appInstScope) {
			String scopeItem = (String)scope;			
			String[] segStrings = scopeItem.split("~");
			if (segStrings.length == 2) {
				if(Integer.parseInt(acctId) >= Integer.parseInt(segStrings[0]) 
						&& Integer.parseInt(acctId) <= Integer.parseInt(segStrings[1])){
					return true;
				}
			} else {
				if(Integer.parseInt(acctId) == Integer.parseInt(segStrings[0]))
					return true;
			}			
		}
		
		return false;
	}
	
	@Override
	public AppService getAppServiceInst(String account){
		if(appInstances != null){
			if(appInstances.containsKey(account)){
				//String appInstId = accountAppInsts.get(account);
	  			return appInstances.get(account);
			}
		}
		return null;
	}
	
	@Override
	public void saveConfig(Future<Void> depFuture){
		saveConfig(getRealServiceName(), depFuture);
	}
	
	@Override
	public void saveConfig(String serviceName, Future<Void> depFuture){
	    String descriptorFile = serviceName + ".json";
	      
	    String cfgFilePath = OtoCloudDirectoryHelper.getConfigDirectory() + descriptorFile;
	    
	    vertx.fileSystem().readFile(cfgFilePath, result -> {
    	    if (result.succeeded()) {
    	    	String fileContent = result.result().toString(); 
    	        
    	    	JsonObject oldCfg = new JsonObject(fileContent);    	    	
    	    	
    	    	if(!oldCfg.containsKey("options")){
    	    		JsonObject optionsCfg = new JsonObject();    	    		
    	    		optionsCfg.put("config", srvCfg);
    	    		oldCfg.put("options", optionsCfg);    	    		
    	    	}else{
    	    		JsonObject optionsCfg = oldCfg.getJsonObject("options");
    	    		optionsCfg.put("config", srvCfg);
    	    	}

				Buffer buffer = JsonUtil.writeToBuffer(oldCfg);	
				
				//持久化服务配置
				vertx.fileSystem().writeFile(cfgFilePath, buffer, handler->{
					if(handler.succeeded()){
						depFuture.complete();
					}else{
						Throwable err = handler.cause();
						logger.error(err.getMessage(), err);
						depFuture.fail(err);
					}
				});	
    	    }else{
				Throwable err = result.cause();
				logger.error(err.getMessage(), err);
				depFuture.fail(err);
    	    }
	    });
		
	}
	
	@Override
	public void undeployComponent(String compName, Future<Void> undepFuture){
		JsonObject srvEngineCfg = srvCfg;
		
		JsonObject instCommonCfg = srvEngineCfg.getJsonObject(AppConfiguration.INST_COMMON);
		
		JsonArray autoDeplCompListCfg = instCommonCfg.getJsonArray("component_deployment");
		int pos = -1;
		for(int i=0;i<autoDeplCompListCfg.size();i++){
			String name = OtoCloudServiceFactory.getServiceName(autoDeplCompListCfg.getJsonObject(i).getString("name"));
			if(compName.equals(name)){
				pos = i;
				break;
			}			
		}
		if(pos >= 0){
			autoDeplCompListCfg.remove(pos);
		}

		instCommonCfg.getJsonObject(OtoConfiguration.COMPONENT_CFG).remove(compName);		
		
		Integer size = appInstances.size();
		if(size > 0){
			AtomicInteger undepCount = new AtomicInteger(0);		
			appInstances.forEach((key,appInst) -> {
				try{
					Future<Void> compDepFuture = Future.future();
					appInst.undeployCompAndUpdateConfig(compName, compDepFuture);
					compDepFuture.setHandler(depRet -> {
	            		if(depRet.failed()){
	                       	Throwable err = depRet.cause();
	                    	logger.error(err.getMessage(), err);
	            		}
	            		undepCompCompletedHandle(undepFuture, undepCount, size);
	            	}); 					
				}catch(Throwable t){
					undepCompCompletedHandle(undepFuture, undepCount, size);
					logger.error(t.getMessage(), t);
				}				
			});		
		}else{
			undepFuture.complete();
		}		
		
	}
	
	private void undepCompCompletedHandle(Future<Void> undepFuture, AtomicInteger depCount, Integer size) {
		if (depCount.incrementAndGet() >= size) {
			Future<Void> innerFuture = Future.future();
			this.saveConfig(getRealServiceName(), innerFuture);
			innerFuture.setHandler(depRet -> {
	    		if(depRet.succeeded()){
	    			undepFuture.complete();		
	    		}else{
	    			Throwable err = depRet.cause();
	    			err.printStackTrace();    
	    			undepFuture.fail(err);
	    			return;
	    		}
	       	});		
					
        }
	}
	
	public void deployEngineComponent(String serviceName, JsonObject compDeploymentDesc, JsonObject compConfig, 
			Future<Void> depFuture){
		super.deployComponent(serviceName, compDeploymentDesc, compConfig, depFuture);
	}
	
	@Override
	public void deployComponent(String serviceName, JsonObject compDeploymentDesc, JsonObject compConfig, 
			Future<Void> depFuture){

		JsonObject srvEngineCfg = srvCfg;
		
		String verticleName = compDeploymentDesc.getString("name");
		String compName = OtoCloudServiceFactory.getServiceName(verticleName);
		
		JsonObject instCommonCfg;
		if(!srvEngineCfg.containsKey(AppConfiguration.INST_COMMON)){
			instCommonCfg = new JsonObject();
			srvEngineCfg.put(AppConfiguration.INST_COMMON, instCommonCfg);
		}else{
			instCommonCfg = srvEngineCfg.getJsonObject(AppConfiguration.INST_COMMON);
		}		
		
		JsonArray autoDeplCompListCfg;
		if(instCommonCfg.containsKey("component_deployment")){
			autoDeplCompListCfg = instCommonCfg.getJsonArray("component_deployment");
		}else{
			autoDeplCompListCfg = new JsonArray();
			instCommonCfg.put("component_deployment", autoDeplCompListCfg);
		}
		autoDeplCompListCfg.add(compDeploymentDesc);
		
		JsonObject compCfgNode;
		if(!instCommonCfg.containsKey(OtoConfiguration.COMPONENT_CFG)){
			compCfgNode = new JsonObject();
			//compCfgNode.mergeIn(compConfig);
			instCommonCfg.put(OtoConfiguration.COMPONENT_CFG, compCfgNode);						
		}else{
			compCfgNode = instCommonCfg.getJsonObject(OtoConfiguration.COMPONENT_CFG);
			//compCfgNode.mergeIn(compConfig);
		}
		if(compCfgNode.containsKey(compName)){
			compCfgNode.getJsonObject(compName).mergeIn(compConfig);
		}else{
			compCfgNode.put(compName, compConfig);
		}
		
		
		Integer size = appInstances.size();
		if(size > 0){
			AtomicInteger depCount = new AtomicInteger(0);		
			appInstances.forEach((key,appInst) -> {
				try{
					Future<Void> compDepFuture = Future.future();
					appInst.deployCompAndUpdateConfig(compDeploymentDesc.copy(), compConfig.copy(), compDepFuture);
					compDepFuture.setHandler(depRet -> {
	            		if(depRet.failed()){
	                       	Throwable err = depRet.cause();
	                    	logger.error(err.getMessage(), err);
	            		}
	            		depCompCompletedHandle(depFuture, depCount, size);
	            	}); 					
				}catch(Throwable t){
					depCompCompletedHandle(depFuture, depCount, size);
					logger.error(t.getMessage(), t);
				}				
			});		
		}else{
			depFuture.complete();
		}		
		
	}
	
	private void depCompCompletedHandle(Future<Void> depFuture, AtomicInteger depCount, Integer size) {
		if (depCount.incrementAndGet() >= size) {
			Future<Void> innerFuture = Future.future();
			this.saveConfig(getRealServiceName(), innerFuture);
			innerFuture.setHandler(depRet -> {
	    		if(depRet.succeeded()){
	    			depFuture.complete();		
	    		}else{
	    			Throwable err = depRet.cause();
	    			err.printStackTrace();    
	    			depFuture.fail(err);
	    			return;
	    		}
	       	});		
					
        }
	}
	
	@Override
	public void saveServiceInstConfig(String instId, JsonObject instCfg, Future<Void> saveFuture){		
		
		JsonObject instCfgs = null;
		if(!srvCfg.containsKey(AppConfiguration.INST_CF_KEY)){
			instCfgs = new JsonObject();
			instCfgs.put(instId, instCfg);			
			srvCfg.put(AppConfiguration.INST_CF_KEY, instCfgs);
		}else{
			instCfgs = srvCfg.getJsonObject(AppConfiguration.INST_CF_KEY);				
			instCfgs.put(instId, instCfg);			
		}

		saveConfig(saveFuture);		
	}

}
