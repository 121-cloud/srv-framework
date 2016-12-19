/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.engine;

import java.util.ArrayList;
import java.util.List;


import java.util.Set;

import com.hazelcast.config.Config;






import otocloud.common.OtoConfiguration;
import otocloud.framework.app.common.AppConfiguration;
import otocloud.framework.app.common.AppInstanceContext;
import otocloud.framework.app.function.ActivityDescriptor;
import otocloud.framework.app.function.AppActivity;
import otocloud.framework.app.function.AppInitActivityImpl;
import otocloud.framework.app.function.BizObjectQueryProxy;
import otocloud.framework.app.persistence.CDODataPersistentPolicy;
import otocloud.framework.app.persistence.CDODataPersistentPolicyFactory;
import otocloud.framework.app.persistence.DataPersistentPolicy;
import otocloud.framework.app.persistence.DataPersistentPolicyFactory;
import otocloud.framework.app.persistence.OtoCloudAppDataSource;
import otocloud.framework.app.persistence.OtoCloudCDODataSource;
import otocloud.framework.common.OtoCloudServiceState;
import otocloud.framework.core.HandlerDescriptor;
import otocloud.framework.core.OtoCloudComponent;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;
import otocloud.framework.core.OtoCloudServiceImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public abstract class AppServiceImpl extends OtoCloudServiceImpl implements AppService, WebServerHost {
	
	//{appid}.appinst_status.changed
	public static final String APPINSTSTATUS_CHANGED_BASE = "platform.appinst_status.changed";//应用实例状态变化订阅地址
    
    protected AppInstanceContext appContext;
    protected OtoCloudAppDataSource otoCloudAppDataSource;
    protected OtoCloudCDODataSource otoCloudCDODataSource;
    
	protected boolean isWebServerHost;
	protected AppWebServerImpl webServer;
	
	protected AppServiceEngine appServiceEngine;

	public AppServiceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AppServiceImpl(String srvName) {
		super(srvName);
		// TODO Auto-generated constructor stub
	}

	public AppServiceImpl(AppServiceEngine appServiceEngine) {
		super();
		this.appServiceEngine = appServiceEngine;
	}

	public void setAppServiceEngine(AppServiceEngine appServiceEngine) {
		this.appServiceEngine = appServiceEngine;
	}

	
	@Override
	public void init(AppInstanceContext appInstCtx, JsonObject instCfg, Vertx activityContainer, Config clusterCfg, JsonObject vertxOptionsCfg, Future<Void> initFuture){		
		this.appContext = appInstCtx;		
		super.init(instCfg, activityContainer, clusterCfg, vertxOptionsCfg, initFuture);
	}
	
	@Override
	public void configService() {
		super.configService();
		
		//setServiceId(appServiceEngine.getServiceId());
		setServiceName(appServiceEngine.getRealServiceName());
		//setServiceName(appContext.getAccount());
		
		appendAppCtx(srvCfg, appContext);		
		if(srvCfg.containsKey(AppConfiguration.WEBSERVER_HOST)){
			isWebServerHost = srvCfg.getBoolean(AppConfiguration.WEBSERVER_HOST);
		}else {
			isWebServerHost = false;
		}
		
		//createAppMongoClient();
	};
	
	@Override
	public AppServiceEngine getAppEngine(){
		return appServiceEngine;
	}
	
	@Override
	public void afterInit(Future<Void> initFuture){

		if(isWebServerHost()){
			webServer = (AppWebServerImpl)createWebServer();
			if(webServer != null){
				webServer.init(appContext.getAccount(), appContext.getAccount(), vertxInstance, 
						srvCfg.getJsonObject(OtoConfiguration.WEBSERVER_CFG), null);
			}						
		}
		
		futureStatusComplete();
	    initFuture.complete();
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
    	if(otoCloudAppDataSource != null)
    		return otoCloudAppDataSource;
    	return appServiceEngine.getAppDatasource();
    }

    @Override
    public OtoCloudCDODataSource getCDODatasource(){
    	if(otoCloudCDODataSource == null)
    		createCDOMongoClient();
    	if(otoCloudCDODataSource != null)
    		return otoCloudCDODataSource;
    	return appServiceEngine.getCDODatasource();
    }
	
	private void appendAppCtx(JsonObject instCfg, AppInstanceContext appInstCtx){
		JsonObject compCommon = null;
		Boolean isNewCommonNode = false;
		if(!instCfg.containsKey(AppConfiguration.COMPONENT_COMMON)){
			compCommon = new JsonObject();
			isNewCommonNode = true;
		}			
		else {
			compCommon = instCfg.getJsonObject(AppConfiguration.COMPONENT_COMMON);
		}		
		
		//将账户上下文加入活动组件公共配置，以便活动组件能取到上下文信息
		compCommon.put(AppConfiguration.APPINST_CONTEXT, appInstCtx.toJson());		
		
		if(isNewCommonNode){			
			instCfg.put(AppConfiguration.COMPONENT_COMMON, compCommon);
		}
	}
	
	@Override
	public List<OtoCloudComponent> createServiceComponents(){	
		List<OtoCloudComponent> retList = new ArrayList<OtoCloudComponent>();
		
		//创建应用初始化活动
		AppInitActivityImpl initActivity = createAppInitActivity();
		if(initActivity != null)
			retList.add(initActivity);
		
		BizObjectQueryProxy bizObjectQueryProxy = new BizObjectQueryProxy();
		retList.add(bizObjectQueryProxy);
		
		List<AppActivity> activityComponents = createBizActivities();
		if(activityComponents != null && activityComponents.size() > 0){
			for (AppActivity activityCom : activityComponents) {
				retList.add(activityCom);
			}
		}
	
		return retList;
	}
	
/*	@Override
	public List<AppActivity> createBizActivities() {
		return null;	
	}*/
	
	@Override
	public String buildEventAddress(String addressBase){										
		return appContext.getAccount() + "." + super.buildEventAddress(addressBase);
	}
	
	@Override
	public void beforeStop(Handler<AsyncResult<Void>> next) {
		Future<Void> ret = Future.future();
		ret.setHandler(next);
		
		if(isWebServerHost() && webServer != null){
			webServer.close();
		}
		
		ret.complete();
	}
	
	@Override
	public void afterStop(io.vertx.core.Future<Void> stopFuture) {
		if(otoCloudAppDataSource != null)
			otoCloudAppDataSource.close();
		
		futureStatusComplete();
		stopFuture.complete();		
	}
	
	
	@Override
	public AppInstanceContext getAppInstContext(){
		return appContext;
	}	
	
	@Override
	public boolean isWebServerHost(){
		return isWebServerHost;
	}
	
	@Override
	public void runWebServer(){
		if(isWebServerHost() && webServer != null){	
			List<ActivityDescriptor> activityDescList = this.getActivityDescriptors();
			webServer.restRoute(activityDescList);

			List<AppService> appInsts =  new ArrayList<AppService>();
			appInsts.add(this);
			webServer.busRoute(appInsts);

			webServer.listen();
		}
	}
	
    
	@Override
    public List<ActivityDescriptor> getActivityDescriptors(){
    	if(components != null){
    		List<ActivityDescriptor> ret = new ArrayList<ActivityDescriptor>();
    		components.forEach((key,component) -> {
    			Set<Verticle> objs = component.getVerticles();
    			if(objs.size() > 0){
    				Object obj = objs.toArray()[0];
	    			if(obj instanceof AppActivity){
	    				ret.add(((AppActivity)obj).getActivityDescriptor());    	
	    			}
    			}
    		});
    		return ret;
    	}
    	return null;
    }
    

	@Override
	public void statusChangedNotify(OtoCloudServiceState previousState,
			OtoCloudServiceState currentState) {		
		JsonArray retArray = new JsonArray();
		JsonObject statusChangeInfo = new JsonObject();
		statusChangeInfo.put("app_id", getRealServiceName());
		statusChangeInfo.put("acct_id", appContext.getAccount());
		statusChangeInfo.put("previous_status", previousState.toString());
		statusChangeInfo.put("current_status", currentState.toString());
		statusChangeInfo.put("current_status_desc", OtoCloudServiceState.toDisplay(currentState));
		retArray.add(statusChangeInfo);
		
    	vertxInstance.eventBus().publish(appContext.getAppId() + "." + APPINSTSTATUS_CHANGED_BASE,	retArray);
	}	
	
	@Override
	public List<OtoCloudEventHandlerRegistry> createHandlers(){ 
		return null;
	}

	
	@Override
	public void saveServiceConfg(String serviceName, Future<Void> depFuture){
		appServiceEngine.saveConfig(depFuture);	    

	}
	
	
	@Override
	public void registerRestAPIs(String compName, List<HandlerDescriptor> handlerDescs, Future<Void> regFuture) {
		appServiceEngine.registerRestAPIs(compName, handlerDescs, regFuture);		
	}
	
	@Override
	public void unregisterRestAPIs(Future<Void> unregFuture){		
		unregFuture.complete();
	}	

	@Override
	public void saveComponentConfig(String component, JsonObject compOpCfg, Future<Void> saveFuture){
		JsonObject compCfg = null;
		if(!srvCfg.containsKey(OtoConfiguration.COMPONENT_CFG)){
			compCfg = new JsonObject();
			compOpCfg = new JsonObject();
			compCfg.put("options", new JsonObject().put("config", compOpCfg));
			
			JsonObject compCateCfg = new JsonObject();
			compCateCfg.put(component, compCfg);
			
			srvCfg.put(OtoConfiguration.COMPONENT_CFG, compCateCfg);
		}else{
			JsonObject compCateCfg = srvCfg.getJsonObject(OtoConfiguration.COMPONENT_CFG);				
			if(compCateCfg.containsKey(component)){
				compCfg = compCateCfg.getJsonObject(component);
				if(compCfg.containsKey("options")){
					JsonObject opCfg = compCfg.getJsonObject("options");
					opCfg.put("config", compOpCfg);
				}else{
					compOpCfg = new JsonObject();
					compCfg.put("options", new JsonObject().put("config", compOpCfg));					
				}					
			}else{
				compCfg = new JsonObject();				
				compCfg.put("options", new JsonObject().put("config", compOpCfg));
				compCateCfg.put(component, compCfg);
			}				
		}

		appServiceEngine.saveServiceInstConfig(appContext.getAccount(), srvCfg, saveFuture);
		
	}

}
