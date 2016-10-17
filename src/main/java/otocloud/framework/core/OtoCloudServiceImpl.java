package otocloud.framework.core;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import otocloud.common.OtoCloudDirectoryHelper;
import otocloud.common.OtoCloudLogger;
import otocloud.common.OtoConfiguration;
import otocloud.common.util.JsonUtil;
import otocloud.framework.app.common.AppConfiguration;
import otocloud.framework.common.OtoCloudServiceLifeCycleImpl;
import otocloud.framework.common.OtoCloudServiceState;
import otocloud.framework.core.factory.OtoCloudComponentFactory;
import otocloud.framework.core.factory.OtoCloudHttpComponentFactory;
import otocloud.framework.core.factory.OtoCloudHttpSecureComponentFactory;
import otocloud.framework.core.factory.OtoCloudMavenComponentFactory;
import otocloud.framework.core.factory.OtoCloudServiceFactory;
import otocloud.persistence.dao.JdbcDataSource;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.Deployment;
import io.vertx.core.impl.DeploymentManager;
import io.vertx.core.impl.VertxImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudServiceImpl extends OtoCloudServiceLifeCycleImpl implements OtoCloudService {
	public static final String REST_URI_REG = "platform.register.rest.to.webserver";
	public static final String REST_URI_UNREG = "platform.unregister.rest.to.webserver";

    protected JsonObject srvCfg;
	protected Vertx vertxInstance;	
	protected JdbcDataSource sysDataSource;
	protected boolean isolationVertx = false;
	protected Map<String, Deployment> components;	
	protected OtoCloudLogger logger;	
	protected String srvId = "";
	protected String srvName = "";
	protected String apiRegServerName = "";
	protected List<OtoCloudEventHandlerRegistry> handlers;
	protected Map<String, String> restAPIRegistryTable;
	protected OtoCloudEventHandlerRegistry apiURIResolver;
	protected JsonObject vertxOptionsCfg;
	protected Config clusterCfg;
	
	public OtoCloudServiceImpl() {
		Map<String, Deployment> compSet = new HashMap<String, Deployment>();
		components = Collections.synchronizedMap(compSet);
		Map<String, String> restRegTb = new HashMap<String, String>();
		restAPIRegistryTable = Collections.synchronizedMap(restRegTb);		
		
	}
	
	public OtoCloudServiceImpl(String srvName) {
		this();
		setServiceName(srvName);
	}

	@Override
	public String getRealServiceName() {
		if(srvName != null && !srvName.isEmpty())
			return srvName;
		return getServiceName();		
	}
	
	@Override
	public String getServiceName() {
		return srvName;
	}
	
	@Override
	public void setServiceName(String srvName) {
		this.srvName = srvName;
	}	

/*	@Override
	public String getServiceId(){
		return srvId;
	}
	
	@Override
	public void setServiceId(String srvId){
		this.srvId = srvId;
	}*/
	
	@Override
	public JdbcDataSource getSysDatasource(){
		if(sysDataSource == null)
			createSysDatasource();
		return sysDataSource;
	}
	
    private void createSysDatasource() {    	
        JsonObject sysDsCfg = srvCfg.getJsonObject(OtoConfiguration.SYS_DATASOURCE, null);  
        if(sysDsCfg != null){
	        sysDataSource = new JdbcDataSource();
	        sysDataSource.init(vertxInstance, sysDsCfg);			
        }        
	}
	
	@Override
	public void registerRestAPIs(String compName, List<HandlerDescriptor> handlerDescs, Future<Void> regFuture){
		if(apiRegServerName.isEmpty()){
			regFuture.complete();		
			return;
		}
		
		Integer size = handlerDescs.size();
		AtomicInteger regCount = new AtomicInteger(0);
		handlerDescs.forEach(handlerDesc->{
			
			String apiName = handlerDesc.getApiName();
			if(!restAPIRegistryTable.containsKey(apiName)){		
				
				restAPIRegistryTable.put(apiName, "");
			
				JsonObject srvRegInfo = new JsonObject()
					.put("address", handlerDesc.getHandlerAddress().getEventAddress())
					.put("method", handlerDesc.getRestApiURI().getHttpMethod().toString())
					.put("messageFormat", handlerDesc.getMessageFormat());
				
				if(apiURIResolver != null){
					srvRegInfo.put("decoratingAddress", apiURIResolver.getRealAddress());
				}
				
				String restApiURI = handlerDesc.getRestApiURI().getUri();
				if(restApiURI.isEmpty()){
					srvRegInfo.put("uri", "/" + getRealServiceName() + "/" + compName);
				}else{
					srvRegInfo.put("uri", "/" + getRealServiceName() + "/" + compName + "/" + restApiURI);
				}
				
				vertxInstance.eventBus().send(apiRegServerName + "." + REST_URI_REG,
						srvRegInfo, ret->{
							if(ret.succeeded()){
								JsonObject retObj = (JsonObject)ret.result().body(); 
								restAPIRegistryTable.put(apiName, retObj.getString("result"));								
							}else{
								restAPIRegistryTable.remove(apiName);
								Throwable err = ret.cause();
								err.printStackTrace();								
							}
							if (regCount.incrementAndGet() >= size) {					
								regFuture.complete();		
								return;
			                }            	
							
				});	
			
			}else{
				if (regCount.incrementAndGet() >= size) {					
					regFuture.complete();		
					return;
                }            	
			}
		});
		
	}
	
	@Override
	public void unregisterRestAPIs(Future<Void> unregFuture){		
		
		if(restAPIRegistryTable.size() > 0){
			JsonArray uriMappingInfos = new JsonArray();
			restAPIRegistryTable.forEach((key, value)->{
				JsonObject srvRegInfo = new JsonObject().put("registerId", value);
				uriMappingInfos.add(srvRegInfo);
			});
			
			vertxInstance.eventBus().send(apiRegServerName + "." + REST_URI_UNREG,
					uriMappingInfos, ret->{
						if(ret.succeeded()){
							unregFuture.complete();
						}else{
							Throwable err = ret.cause();
							err.printStackTrace();
							unregFuture.fail(err);
						}
						
			});	
			
			restAPIRegistryTable.clear();
			
		}else{
			unregFuture.complete();
		}
	}	
	


	
	//使用外部容器vertx
	@Override
	public void init(JsonObject srvCfg, Vertx compContainer, Config clusterCfg, JsonObject vertxOptionsCfg, Future<Void> initFuture){		
		statusReset();
		setFutureStatus(OtoCloudServiceState.INITIALIZED);	
				
		beforeInit(next->{
			if(next.succeeded()){		
				this.srvCfg = srvCfg;
				this.vertxInstance = compContainer;		
				this.clusterCfg = clusterCfg;
				this.vertxOptionsCfg = vertxOptionsCfg;
				
				configService();
				
				//createSysDatasource();
				
				registerHandlers();
				
				afterInit(initFuture);
			}else{
				this.futureStatusRollback();
				initFuture.fail(next.cause());
			}
		});
	}

	//创建独立vertx	
	@Override
	public void init(JsonObject srvCfg, Config clusterCfg, JsonObject vertxOptionsCfg, Future<Void> initFuture){		
		isolationVertx = true;		
		VertxOptions options = OtoCloudServiceImpl.createVertxOptions(srvCfg, clusterCfg, vertxOptionsCfg);		
		// 创建群集Vertx运行时环境
		Vertx.clusteredVertx(options, res -> {
			// 运行时创建完后初始化
			if (res.succeeded()) {
				// 创建vertx实例
				Vertx newVertx = res.result();	
				
				registerComponentFactory(newVertx);	
				
				init(srvCfg, newVertx, clusterCfg, vertxOptionsCfg, initFuture);	

			} else {

				Throwable err = res.cause();
    	    	initFuture.fail(err);
    	    	logger.error(err.getMessage(), err);
    	    }
		});		

	}
	
	@Override
	public void configService(){
		
        //setServiceId(srvCfg.getString("service_id", ""));
        setServiceName(srvCfg.getString("service_name", ""));
        if(srvCfg.containsKey("api_register_server")){
        	apiRegServerName = srvCfg.getJsonObject("api_register_server").getString("webserver_name", "");
        }
        
		configLogging();
	}
	
	private void configLogging() {
		logger = new OtoCloudLogger(getRealServiceName(), LoggerFactory.getLogger(this.getClass().getName()));
	}	

	@Override
	public void run(Future<Void> runFuture) throws Exception{
		if(stateChangeIsReady() == false){
			runFuture.fail("实例:[" + getRealServiceName() + "]状态变化还未完成，不允许run");
			return;
		}
			
		if(currentState == OtoCloudServiceState.INITIALIZED
				|| currentState == OtoCloudServiceState.STOPPED){ 	
			
			setFutureStatus(OtoCloudServiceState.RUNNING);
			
			beforeRun(next->{
				if(next.succeeded()){		
					
					List<OtoCloudComponent> verticles = createServiceComponents();
					
					if(verticles != null && verticles.size() > 0){
						Integer firstComponentIndex = 0;
						Future<Void> innerRunFuture = Future.future();
						startupVerticleComponent(firstComponentIndex, verticles.size(), verticles, innerRunFuture);
						innerRunFuture.setHandler(ret -> {
				    		if(ret.succeeded()){
				    			if(srvCfg.containsKey("component_deployment")){
				    				JsonArray autoDeplCompListCfg = srvCfg.getJsonArray("component_deployment");
				    				
					    			if(autoDeplCompListCfg != null && autoDeplCompListCfg.size() > 0){		    				
					    				Integer startIndex = 0;
					    				Future<Void> depFuture = Future.future();
					    				deployExtraComp(autoDeplCompListCfg, startIndex, autoDeplCompListCfg.size(), depFuture);
					    				depFuture.setHandler(depRet -> {
					    		    		if(depRet.failed()){
					                           	Throwable err = depRet.cause();
					                        	logger.error(err.getMessage(), err);
					    		    		}
							    			afterRun(runFuture);
					    				});		    				
					    			}else{		    			
					    				afterRun(runFuture);
					    			}
				    			}else{
				    				afterRun(runFuture);
				    			}
				    		}else{	
				    			if(srvCfg.containsKey("component_deployment")){
				    				JsonArray autoDeplCompListCfg = srvCfg.getJsonArray("component_deployment");

					    			if(autoDeplCompListCfg != null && autoDeplCompListCfg.size() > 0){		    				
					    				Integer startIndex = 0;
					    				Future<Void> depFuture = Future.future();
					    				deployExtraComp(autoDeplCompListCfg, startIndex, autoDeplCompListCfg.size(), depFuture);
					    				depFuture.setHandler(depRet -> {
					    		    		if(depRet.failed()){
					                           	Throwable err = depRet.cause();
					                        	logger.error(err.getMessage(), err);
	
								    			futureStatusRollback();
								    			runFuture.fail(ret.cause());
					    		    		}else{
					    		    			afterRun(runFuture);
					    		    		}
					    				});		    				
					    			}else{ 					    			
	
						    			futureStatusRollback();
						    			runFuture.fail(ret.cause());
					    			}
				    			}else{
			
					    			futureStatusRollback();
					    			runFuture.fail(ret.cause());		    				
				    			}		    			
				    		}
						});				
					}else{
						
		    			if(srvCfg.containsKey("component_deployment")){
		    				JsonArray autoDeplCompListCfg = srvCfg.getJsonArray("component_deployment");
		    				
			    			if(autoDeplCompListCfg != null && autoDeplCompListCfg.size() > 0){		    				
			    				Integer startIndex = 0;
			    				Future<Void> depFuture = Future.future();
			    				deployExtraComp(autoDeplCompListCfg, startIndex, autoDeplCompListCfg.size(), depFuture);
			    				depFuture.setHandler(depRet -> {
			    		    		if(depRet.failed()){
			                           	Throwable err = depRet.cause();
			                        	logger.error(err.getMessage(), err);
			                        	
						    			futureStatusRollback();
						    			runFuture.fail(err);	
			                        	
			    		    		}else{
			    		    			afterRun(runFuture);
			    		    		}
			    				});		    				
			    			}else{		    			
			    				afterRun(runFuture);
			    			}
		    			}else{
		    				afterRun(runFuture);
		    			}		

					}					

				}else{
					this.futureStatusRollback();
					runFuture.fail(next.cause());
				}
			});
			
		}else{
			runFuture.fail("实例:[" + getRealServiceName() + "]状态为:" + currentState.toString() + ",不能run");
		}	

	}	
	

	private void deployExtraComp(JsonArray autoDeplCompListCfg, Integer startIndex, Integer size, Future<Void> depFuture) {
		JsonObject depConfig = autoDeplCompListCfg.getJsonObject(startIndex);
		
		String verticleName = depConfig.getString("name");
		
		JsonObject depOptionObj = new JsonObject();		
		JsonObject cfgNode = new JsonObject();
		cfgNode.put("serviceConfig", srvCfg);		
		depOptionObj.put("config", cfgNode);		
		
		OtoCloudCompDepOptions deploymentOptions = new OtoCloudCompDepOptions(this);
		deploymentOptions.fromJson(depOptionObj);

		vertxInstance.deployVerticle(verticleName, deploymentOptions,
			res -> {				
					if (res.succeeded()) {
						String deploymentID = res.result();
						String compName = OtoCloudServiceFactory.getServiceName(verticleName);
						components.put(compName, ((VertxImpl)vertxInstance).getDeployment(deploymentID));
						logger.info("Component:[" + compName + "] deploy completed! service:[" + getRealServiceName() + "] 's component number=" + components.size());
					} else {
						Throwable err = res.cause();
						logger.error(err.getMessage(), err);
					}
					Integer nextIdx = startIndex + 1;
					if (nextIdx < size)
						deployExtraComp(autoDeplCompListCfg, nextIdx, size, depFuture);
					else if (nextIdx >= size) {
						depFuture.complete();
					}
			});
	}
	
	
	public void saveServiceConfg(String serviceName, Future<Void> depFuture){
	    String descriptorFile = serviceName + ".json";
	      
	    String cfgFilePath = OtoCloudDirectoryHelper.getConfigDirectory() + descriptorFile;
	    
	    vertxInstance.fileSystem().readFile(cfgFilePath, result -> {
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
				vertxInstance.fileSystem().writeFile(cfgFilePath, buffer, handler->{
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

		saveServiceConfg(this.getRealServiceName(), saveFuture);		
	}
	
	@Override
	public void deployComponent(String serviceName, JsonObject compDeploymentDesc, JsonObject compConfig, Future<Void> depFuture) {
		Future<Void> innerFuture = Future.future();
		this.deployCompAndUpdateConfig(compDeploymentDesc, compConfig, innerFuture);
		innerFuture.setHandler(depRet -> {
    		if(depRet.succeeded()){
    			saveServiceConfg(serviceName, depFuture);    			
    		}else{
    			Throwable err = depRet.cause();
    			err.printStackTrace();    
    			depFuture.fail(err);
    			return;
    		}
       	});		
		
	}
	
	@Override
	public void deployCompAndUpdateConfig(JsonObject compDeploymentDesc, JsonObject compConfig, Future<Void> depFuture) {

		String verticleName = compDeploymentDesc.getString("name");
		String compName = OtoCloudServiceFactory.getServiceName(verticleName);
		
		JsonArray autoDeplCompListCfg;
		if(srvCfg.containsKey("component_deployment")){
			autoDeplCompListCfg = srvCfg.getJsonArray("component_deployment");
		}else{
			autoDeplCompListCfg = new JsonArray();
			srvCfg.put("component_deployment", autoDeplCompListCfg);
		}
		autoDeplCompListCfg.add(compDeploymentDesc);
		
		JsonObject compCfgNode;
		if(!srvCfg.containsKey(OtoConfiguration.COMPONENT_CFG)){
			compCfgNode = new JsonObject();
			//compCfgNode.mergeIn(compConfig);
			srvCfg.put(OtoConfiguration.COMPONENT_CFG, compCfgNode);						
		}else{
			compCfgNode = srvCfg.getJsonObject(OtoConfiguration.COMPONENT_CFG);
			//compCfgNode.mergeIn(compConfig);
		}
		if(compCfgNode.containsKey(compName)){
			compCfgNode.getJsonObject(compName).mergeIn(compConfig);
		}else{
			compCfgNode.put(compName, compConfig);
		}
		
		
		JsonObject depOptionObj = new JsonObject();
		JsonObject cfgNode = new JsonObject();
		cfgNode.put("serviceConfig", srvCfg);		
		depOptionObj.put("config", cfgNode);		
			
		OtoCloudCompDepOptions deploymentOptions = new OtoCloudCompDepOptions(this);
		deploymentOptions.fromJson(depOptionObj);

		vertxInstance.deployVerticle(verticleName, deploymentOptions,
			res -> {				
				if (res.succeeded()) {
					String deploymentID = res.result();
					//String compName = OtoCloudServiceFactory.getServiceName(verticleName);
					components.put(compName, ((VertxImpl)vertxInstance).getDeployment(deploymentID));
					logger.info("Component:[" + compName + "] deploy completed! service:[" + getRealServiceName() + "] 's component number=" + components.size());
					depFuture.complete();
				} else {
					Throwable err = res.cause();
					logger.error(err.getMessage(), err);
					depFuture.fail(err);
				}				
			});		
		
	}
	
	@Override
	public void undeployComponent(String compName, Future<Void> undepFuture) {
		
		Future<Void> innerFuture = Future.future();
		this.undeployCompAndUpdateConfig(compName, innerFuture);
		innerFuture.setHandler(depRet -> {
    		if(depRet.succeeded()){
    			saveServiceConfg(this.getRealServiceName(), undepFuture);			
    		}else{
    			Throwable err = depRet.cause();
    			err.printStackTrace();    
    			undepFuture.fail(err);
    			return;
    		}
       	});			

	}	
	
	@Override
	public void undeployCompAndUpdateConfig(String compName, Future<Void> undepFuture){
		if(!components.containsKey(compName)){
			undepFuture.complete();
			return;
		}
		
		JsonArray autoDeplCompListCfg = srvCfg.getJsonArray("component_deployment");
		int pos = -1;
		boolean isolationJar = false;
		for(int i=0;i<autoDeplCompListCfg.size();i++){
			JsonObject depCompCfg = autoDeplCompListCfg.getJsonObject(i);
			String name = OtoCloudServiceFactory.getServiceName(depCompCfg.getString("name"));
			if(compName.equals(name)){
				isolationJar = true;
				pos = i;
				break;
			}			
		}
		if(pos >= 0){
			autoDeplCompListCfg.remove(pos);
		}
		
		boolean needUnloadJar = isolationJar ? true : false;

		srvCfg.getJsonObject(OtoConfiguration.COMPONENT_CFG).remove(compName);
		
		Deployment deployment = components.get(compName);
			
		String deploymentId = deployment.deploymentID();
		
		vertxInstance.undeploy(deploymentId,res -> {
			if(res.succeeded()){	
				
				if(needUnloadJar){
					Future<Void> unLoadFuture = Future.future();
					unLoadComponentJar(vertxInstance, deployment, unLoadFuture);
					unLoadFuture.setHandler(unLoadRet -> {
			    		if(unLoadRet.failed()){
			    			Throwable err = unLoadRet.cause();
			    			err.printStackTrace();    
			    		}
			    		
						components.remove(compName);					
						
						logger.info("component: [" + compName + "] undeploy!");
						undepFuture.complete();
			    		
			       	});			
				}else{				
					components.remove(compName);
					logger.info("component: [" + compName + "] undeploy!");
					undepFuture.complete();
				}
			}else{					
               	Throwable err = res.cause();
               	err.printStackTrace();
            	logger.error(err.getMessage(), err);
            	undepFuture.fail(err);
    		}
		});
		
	}
	
	//@SuppressWarnings("unchecked")
	public static void unLoadComponentJar(Vertx vertxInst, Deployment deployment, Future<Void> unLoadFuture){
		String isolationGroup = deployment.deploymentOptions().getIsolationGroup();
        Field depMgrField;
		try {
			depMgrField = VertxImpl.class.getDeclaredField("deploymentManager");
	        depMgrField.setAccessible(true);
	        try {
				DeploymentManager depMgr = (DeploymentManager)depMgrField.get(vertxInst);
				Field classloadersField = DeploymentManager.class.getDeclaredField("classloaders");
				classloadersField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, ClassLoader> classloaders = (Map<String, ClassLoader>)classloadersField.get(depMgr);
				URLClassLoader classLoader = (URLClassLoader)classloaders.get(isolationGroup);
				try {

					classLoader.close();					
					classloaders.remove(isolationGroup);
					classLoader = null;
					System.gc();
								
					System.out.println("unload jar:" + isolationGroup + " completed!");
					
					unLoadFuture.complete();
				} catch (IOException e) {
					classloaders.remove(isolationGroup);
					// TODO Auto-generated catch block
					e.printStackTrace();
					unLoadFuture.fail(e);
				}				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				unLoadFuture.fail(e);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			unLoadFuture.fail(e);
		} 
	}
	
	public static ClassLoader getClassLoader(Vertx vertxInst, Deployment deployment){
		String isolationGroup = deployment.deploymentOptions().getIsolationGroup();
        Field depMgrField;
		try {
			depMgrField = VertxImpl.class.getDeclaredField("deploymentManager");
	        depMgrField.setAccessible(true);
	        try {
				DeploymentManager depMgr = (DeploymentManager)depMgrField.get(vertxInst);
				Field classloadersField = DeploymentManager.class.getDeclaredField("classloaders");
				classloadersField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Map<String, ClassLoader> classloaders = (Map<String, ClassLoader>)classloadersField.get(depMgr);
				return classloaders.get(isolationGroup);				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
	
/*	@Override
	public ClassLoader getComponentClassLoader(String compName) {
		if(!components.containsKey(compName)){			
			return null;
		}
		
		JsonArray autoDeplCompListCfg = srvCfg.getJsonArray("component_deployment");
		int pos = -1;
		boolean isolationJar = false;
		for(int i=0;i<autoDeplCompListCfg.size();i++){
			JsonObject depCompCfg = autoDeplCompListCfg.getJsonObject(i);
			String name = OtoCloudServiceFactory.getServiceName(depCompCfg.getString("name"));
			if(compName.equals(name)){
				isolationJar = true;
				pos = i;
				break;
			}			
		}
		if(pos >= 0){
			autoDeplCompListCfg.remove(pos);
		}
		
		if(!isolationJar)
			return null;
		
		Deployment deployment = components.get(compName);			
		return getClassLoader(vertxInstance, deployment);
	}*/
	
	public static JsonArray buildClassPaths(JsonArray classpaths){
		JsonArray retArray = new JsonArray();		
		String jarPath = OtoCloudDirectoryHelper.getLibDirectory();		
		classpaths.forEach(classpath->{
			String jarFileURL = jarPath + classpath;
			retArray.add(jarFileURL);
		});
		return retArray;
	}
	
	public static List<String> buildClassPathToList(List<String> classpaths){
		List<String> retArray = new ArrayList<String>();	
		String jarPath = OtoCloudDirectoryHelper.getLibDirectory();		
		for(String classpath : classpaths){
			String jarFileURL = jarPath + classpath;
			retArray.add(jarFileURL);
		}
		return retArray;
	}
	
	@Override
	public void close(Future<Void> stopFuture) throws Exception{
		if(stateChangeIsReady() == false){
			stopFuture.fail("实例:[" + getRealServiceName() + "]状态变化还未完成，不允许进行stop");
			return;
		}
		if(currentState != OtoCloudServiceState.RUNNING){
			stopFuture.fail("实例:[" + getRealServiceName() + "]状态必须为:RUNNING,才能stop");
			return;
		}	
		
		setFutureStatus(OtoCloudServiceState.STOPPED);
		
		beforeStop(next->{
			if(next.succeeded()){
				
				Future<Void> unregApiFuture = Future.future();
				unregisterRestAPIs(unregApiFuture);				
				unregApiFuture.setHandler(unregRet -> {
            		if(unregRet.succeeded()){ 
            			
            			if(isolationVertx){		
            				unRegisterComponentFactory(vertxInstance);
            				vertxInstance.close(closeHandler->{
            					if(sysDataSource != null)
            						sysDataSource.close();
            					if(closeHandler.succeeded()){
            						components.clear();
            						afterStop(stopFuture);   
            					}else{
                        			Throwable err = closeHandler.cause();
                        			err.printStackTrace(); 
                    				futureStatusRollback();
                    				stopFuture.fail(err);
            					}
            				});
            			}else{            				

							if(components.size() > 0){
								Future<Void> undepCompFuture = Future.future();
								undeployComponents(undepCompFuture);
								undepCompFuture.setHandler(undepCompRet -> {
						    		if(undepCompRet.failed()){
		            					if(sysDataSource != null)
		            						sysDataSource.close();
						    			futureStatusRollback();
						               	Throwable err = undepCompRet.cause();
						               	err.printStackTrace();
						            	//logger.error(err.getMessage(), err);
						            	stopFuture.fail(err);
						    		}else{
						    			closeCompletedHandle(stopFuture);
						    		}
								});        					
								
							}else{
								closeCompletedHandle(stopFuture);
							}
            			}
				
               		}else{
    					if(sysDataSource != null)
    						sysDataSource.close();
               			futureStatusRollback();
            			Throwable err = unregRet.cause();
            			err.printStackTrace(); 
            			stopFuture.fail(err);            			
            		}  		         		
               	});	               	
				

			}else{
				if(sysDataSource != null)
					sysDataSource.close();
				this.futureStatusRollback();
				stopFuture.fail(next.cause());
			}
		});
		
		
	}
	
	private void undeployComponents(Future<Void> stopFuture){
		Integer size = components.size();
		AtomicInteger stoppedCount = new AtomicInteger(0);
		
		components.forEach((key, comp)-> {
			if(!comp.isChild()){
				vertxInstance.undeploy(comp.deploymentID(), res -> {
					if(res.succeeded()){				
						
					}else{					
                       	Throwable err = res.cause();
                    	logger.error(err.getMessage(), err);
            		}
            		if (stoppedCount.incrementAndGet() >= size) {
            			//closeCompletedHandle(stopFuture);
            			stopFuture.complete();
                    }            		
				});
			}else{
        		if (stoppedCount.incrementAndGet() >= size) {					            			
        			//closeCompletedHandle(stopFuture);
        			stopFuture.complete();
                }
			}
		});
	}
	
	public void closeCompletedHandle(Future<Void> stopFuture){
		components.clear();
		Future<Void> unregHandlersFuture = Future.future();
		unregisterHandlers(unregHandlersFuture);				
		unregHandlersFuture.setHandler(unregRet -> {
    		if(unregRet.succeeded()){   
    			if(handlers != null && !handlers.isEmpty())
    				handlers.clear();    			
    			Future<Void> afterStopFuture = Future.future();
        		afterStop(afterStopFuture);        		
        		afterStopFuture.setHandler(afterStopRet -> {
    				if(sysDataSource != null)
    					sysDataSource.close();
            		if(afterStopRet.succeeded()){   
            			stopFuture.complete();
            		}else{
            			stopFuture.fail(afterStopRet.cause());
            		}
               	});	
        		
    		}else{
				if(sysDataSource != null)
					sysDataSource.close();
    			Throwable err = unregRet.cause();
    			err.printStackTrace(); 
				futureStatusRollback();
				stopFuture.fail(err);
    		}
       	});	

	}
	
	//递归启动Verticle组件
	private void startupVerticleComponent(Integer componentIndex, Integer size, List<OtoCloudComponent> verticles, Future<Void> runFuture) {

		OtoCloudComponent otoCloudComponent = verticles.get(componentIndex);

		OtoCloudCompDepOptions deploymentOptions = new OtoCloudCompDepOptions(this);		

		String compName = otoCloudComponent.getName();
		JsonObject compOpsCfg = OtoCloudComponentFactory.getComponentConfig(srvCfg, compName).getJsonObject("options");;			
		
		deploymentOptions.fromJson(compOpsCfg);

		vertxInstance.deployVerticle(otoCloudComponent, deploymentOptions,
			res -> {				
					if (res.succeeded()) {
						String deploymentID = res.result();
						components.put(compName, ((VertxImpl)vertxInstance).getDeployment(deploymentID));		
						logger.info("Component:[" + compName + "] deploy completed! service:[" + getRealServiceName() + "] 's component number=" + components.size());

					} else {
						Throwable err = res.cause();
						logger.error(err.getMessage(), err);
					}
					Integer nextIdx = componentIndex + 1;					
					//Integer size = verticles.size();
					if (nextIdx < size)
						startupVerticleComponent(nextIdx, size, verticles, runFuture);
					else if (nextIdx >= size) {
						runFuture.complete();
					}
			});

	}	
	
	
	@Override
	public void futureStatusComplete() {
		super.futureStatusComplete();		
		logger.info("instance latest status:" + getCurrentStatus().toString());
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void futureStatusRollback(){
		super.futureStatusRollback();		
		logger.info("instance rollback latest status:" + getCurrentStatus().toString());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeInit(Handler<AsyncResult<Void>> next) {
		Future<Void> ret = Future.future();
		ret.setHandler(next);		
		ret.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterInit(Future<Void> initFuture) {
		futureStatusComplete();
	    initFuture.complete();	
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeRun(Handler<AsyncResult<Void>> next) {
		Future<Void> ret = Future.future();
		ret.setHandler(next);		
		ret.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterRun(Future<Void> runFuture) {
		futureStatusComplete();
		runFuture.complete();			
	}
	



	/**
	 * {@inheritDoc}
	 */
	@Override
	public void beforeStop(Handler<AsyncResult<Void>> next) {
		Future<Void> ret = Future.future();
		ret.setHandler(next);		
		ret.complete();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterStop(Future<Void> stopFuture) {
		futureStatusComplete();
		stopFuture.complete();			
	}
	
	@Override
	public Map<String, Deployment> getComponents(){
		return components;
	}
	
	@Override
	public EventBus getBus(){
		return vertxInstance.eventBus();
	}
	
	private void registerHandlers(){
		handlers = createHandlers();
		if(handlers != null && handlers.size() > 0){
			EventBus bus = getBus();
			handlers.forEach(value -> {
				value.register(bus);
			});
		}	
	}
	
	private void unregisterHandlers(Future<Void> unregFuture) {
		if(handlers != null && handlers.size() > 0){
			Integer size = handlers.size();
			AtomicInteger stoppedCount = new AtomicInteger(0);			
			handlers.forEach(value -> {
				Future<Void> singleUnregFuture = Future.future();
				value.unRegister(singleUnregFuture);				
				singleUnregFuture.setHandler(unregRet -> {
            		if(unregRet.succeeded()){   

            		}else{
            			Throwable err = unregRet.cause();
            			err.printStackTrace(); 
            			unregFuture.fail(err);
            			return;
            		}
            		
               		if (stoppedCount.incrementAndGet() >= size) {         			
               			unregFuture.complete();
                    }         		
            		
               	});	               	
			});
		}else{
			unregFuture.complete();
		}

	}
	
	@Override
	public List<OtoCloudEventHandlerRegistry> createHandlers(){ 
		List<OtoCloudEventHandlerRegistry> ret = new ArrayList<OtoCloudEventHandlerRegistry>();
		
		OtoCloudEventHandlerRegistry compDeploymentHandler = new CompDeploymentHandler(this);
		ret.add(compDeploymentHandler);
		
		OtoCloudEventHandlerRegistry compUndeploymentHandler = new CompUndeploymentHandler(this);			
		ret.add(compUndeploymentHandler);
			
		return ret;
	}

	@Override
	public String buildEventAddress(String addressBase){
		return getRealServiceName() + "." + addressBase;
	}
	
	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger.getLogger();
	}

	
	public static void registerComponentFactory(Vertx vertxInstance){		
		OtoCloudComponentFactory serviceFactory = new OtoCloudComponentFactory();	
		vertxInstance.registerVerticleFactory(serviceFactory);		
		
		OtoCloudHttpComponentFactory httpServiceFactory = new OtoCloudHttpComponentFactory();	
		vertxInstance.registerVerticleFactory(httpServiceFactory);		
		
		OtoCloudHttpSecureComponentFactory httpSecureServiceFactory = new OtoCloudHttpSecureComponentFactory();	
		vertxInstance.registerVerticleFactory(httpSecureServiceFactory);	
		
		OtoCloudMavenComponentFactory mavenComponentFactory = new OtoCloudMavenComponentFactory();
		vertxInstance.registerVerticleFactory(mavenComponentFactory);
	}

	
	public static void unRegisterComponentFactory(Vertx vertxInstance){
		Set<VerticleFactory> verticleFactories = vertxInstance.verticleFactories();
		if(verticleFactories != null && verticleFactories.size() > 0){
			for(VerticleFactory verticleFactory : verticleFactories){
				vertxInstance.unregisterVerticleFactory(verticleFactory);
			}			
		}
	}
	
	private static String getLocalHostAddress(){
		try{
			InetAddress addr = InetAddress.getLocalHost();	
			String ip = addr.getHostAddress();//获得本机IP
			return ip;
		}catch(Exception e){	
			e.printStackTrace();
			return "localhost";
		}
	}
	
	
	public static VertxOptions createVertxOptions(JsonObject srvCfg, Config clusterCfg, JsonObject vertxOptions){
		VertxOptions options = null;
		if(vertxOptions != null){
			options = new VertxOptions(vertxOptions);
		} else{
			options = new VertxOptions();
		}
		options.setClustered(true);
		ClusterManager mgr = options.getClusterManager();
		if (mgr == null) {
			if(clusterCfg == null){
				mgr = new HazelcastClusterManager();
				options.setClusterManager(mgr);	
				System.out.println("使用本地分布式集群！");
			}else{			
	   	        String clusterHost = "localhost";
		        int clusterPort = -1;
		        if(srvCfg.containsKey(OtoConfiguration.CLUSTER_CFG)){		
		        	JsonObject clusterCfgObj = srvCfg.getJsonObject(OtoConfiguration.CLUSTER_CFG);
			        if(clusterCfgObj.containsKey(OtoConfiguration.CLUSTER_HOST)){
			        	clusterHost = clusterCfgObj.getString(OtoConfiguration.CLUSTER_HOST);
			        }else{
			        	clusterHost = getLocalHostAddress();
			        }
			        if(clusterCfgObj.containsKey(OtoConfiguration.CLUSTER_PORT)){
			        	clusterPort = clusterCfgObj.getInteger(OtoConfiguration.CLUSTER_PORT);
			        }
		        }
		        
		        System.out.println("加入分布式集群的本机IP为:" + clusterHost);
		        
				//分布式部署必须设置主机名/IP
				options.setClusterHost(clusterHost);
				if(clusterPort > 0){
					options.setClusterPort(clusterPort);
				}
	
				try{
	/*				String clusterCfgFilePath = System.getProperty("user.dir") + "/conf/" + clusterCfgFile; //hazelcast.xml";
					Config clusterCfg = new FileSystemXmlConfig(clusterCfgFilePath);
	*/				
					mgr = new HazelcastClusterManager(clusterCfg);
					options.setClusterManager(mgr);	
				}catch(Exception e){
					//logger.error(e.getMessage(), e.getCause());		
					e.printStackTrace();
				}
			}
		}
		return options;
	}	
	
	
	public static void internalMain(String logFile, String srvCfgFile, OtoCloudServiceImpl srvRunner){
		
    	//响应进程退出
    	Runtime runtime = Runtime.getRuntime();  
    	Thread thread = new Thread(new ServiceShutDownListener(srvRunner));  
    	runtime.addShutdownHook(thread);  
		
		Config clusterCfg = config(logFile);
		
		String cfgFilePath = OtoCloudDirectoryHelper.getConfigDirectory() + srvCfgFile;			
			
		Vertx.vertx().fileSystem().readFile(cfgFilePath, result -> {
    	    if (result.succeeded()) {
    	    	String fileContent = result.result().toString(); 
    	        System.out.println(fileContent);
    	        JsonObject srvCfg = new JsonObject(fileContent);
    	        
    	        JsonObject innerConfig = srvCfg.getJsonObject("options").getJsonObject("config");
    	        
    	        JsonObject vertxOptionsCfg = innerConfig.getJsonObject(AppConfiguration.VERTX_OPTIONS_KEY, null);
    	        
    	        Future<Void> initFuture = Future.future();
    	        srvRunner.init(innerConfig, clusterCfg, vertxOptionsCfg, initFuture);
    	        initFuture.setHandler(ret -> {
    	    		if(ret.succeeded()){   
    	    			try{
    		    			Future<Void> runFuture = Future.future();
    		    			//运行
    		    			srvRunner.run(runFuture);
    		    			runFuture.setHandler(runRet -> {
    		            		if(runRet.succeeded()){   
    		            			System.out.println("running...");	            			
    		            		}else{
    		            			Throwable err = runRet.cause();
    		            			System.err.println("run failed" + err);
    		            		}
    		               	});	               	
    	    			}catch(Throwable t){        			
    	        			System.err.println("run failed" + t);
    	        		}
    	    		}else{
    	    			Throwable err = ret.cause();
    	    			System.err.println("initialize failed" + err);
    	    		}	
        			
    			});	    	        
    	        
    	    } else {
    	        System.err.println(srvCfgFile + "file not found" + result.cause());    	        
    	    }	
		});

	}
	
	
	public static void internalMain(String logFile, String srvCfgFile, OtoCloudServiceImpl srvRunner, Future<Void> runFuture){
		
    	//响应进程退出
    	Runtime runtime = Runtime.getRuntime();  
    	Thread thread = new Thread(new ServiceShutDownListener(srvRunner));  
    	runtime.addShutdownHook(thread);  

		
		Config clusterCfg = config(logFile);
		
		String cfgFilePath = OtoCloudDirectoryHelper.getConfigDirectory() + srvCfgFile;			
			
		Vertx.vertx().fileSystem().readFile(cfgFilePath, result -> {
    	    if (result.succeeded()) {
    	    	String fileContent = result.result().toString(); 
    	        System.out.println(fileContent);
    	        JsonObject srvCfg = new JsonObject(fileContent);
    	        
    	        JsonObject innerConfig = srvCfg.getJsonObject("options").getJsonObject("config");
    	        
    	        JsonObject vertxOptionsCfg = innerConfig.getJsonObject(AppConfiguration.VERTX_OPTIONS_KEY, null);
    	        
    	        Future<Void> initFuture = Future.future();
    	        srvRunner.init(innerConfig, clusterCfg, vertxOptionsCfg, initFuture);
    	        initFuture.setHandler(ret -> {
    	    		if(ret.succeeded()){   
    	    			try{
    		    			//运行
    		    			srvRunner.run(runFuture);

    	    			}catch(Throwable t){        			
    	        			System.err.println("run failed" + t);
    	        		}
    	    		}else{
    	    			Throwable err = ret.cause();
    	    			System.err.println("initialize failed" + err);
    	    		}	
        			
    			});	    	        
    	        
    	    } else {
    	        System.err.println(srvCfgFile + "file not found" + result.cause());    	        
    	    }	
		});

	}	
	
	public static void createVertxInstance(String logFile, String srvCfgFile, Future<Vertx> createFuture){		
		
		Config clusterCfg = config(logFile);
		
		String cfgFilePath = OtoCloudDirectoryHelper.getConfigDirectory() + srvCfgFile;			
			
		Vertx.vertx().fileSystem().readFile(cfgFilePath, result -> {
    	    if (result.succeeded()) {
    	    	String fileContent = result.result().toString(); 
    	        System.out.println(fileContent);
    	        JsonObject srvCfg = new JsonObject(fileContent);
    	        
    	        JsonObject innerConfig = srvCfg.getJsonObject("options").getJsonObject("config");
    	        
    	        JsonObject vertxOptionsCfg = innerConfig.getJsonObject(AppConfiguration.VERTX_OPTIONS_KEY, null);

    			VertxOptions options = OtoCloudServiceImpl.createVertxOptions(srvCfg, clusterCfg, vertxOptionsCfg);		
    			// 创建群集Vertx运行时环境
    			Vertx.clusteredVertx(options, res -> {
    				// 运行时创建完后初始化
    				if (res.succeeded()) {
    					// 创建vertx实例
    					Vertx newVertx = res.result();    					
    					createFuture.complete(newVertx);
    				} else {
    					Throwable err = res.cause();
    					createFuture.fail(err);
    	    	    }
    			});		
    	        
    	        
    	    } else {
    	        System.err.println(srvCfgFile + "file not found" + result.cause());    	     
    	        createFuture.fail(result.cause());
    	    }	
		});



	}
	
	
	private static Config config(String logConfigFile) {
		//logging配置
		String logFile = "log4j2.xml";
		if(logConfigFile != null && !logConfigFile.isEmpty()){
			logFile = logConfigFile;
		}
		String logCfgFilePath = "file:/" + OtoCloudDirectoryHelper.getConfigDirectory() + logFile; //log4j2.xml"; 
		System.out.println(logCfgFilePath);
		System.setProperty("log4j.configurationFile", logCfgFilePath);		
		System.setProperty(LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME, "io.vertx.core.logging.SLF4JLogDelegateFactory");
		
		return loadClusterConfig();
	}

	
	public static Config loadClusterConfig() {
		//bus群集配置
		try{			
			String clusterCfgFilePath = OtoCloudDirectoryHelper.getConfigDirectory() + AppConfiguration.CLUSTER_CFG_FILE; //hazelcast.xml";
			return new FileSystemXmlConfig(clusterCfgFilePath);
		}catch(Exception e){
			return null;
			//e.printStackTrace();
		}
    }

}
