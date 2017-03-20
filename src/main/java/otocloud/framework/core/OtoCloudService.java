package otocloud.framework.core;


import java.util.List;
import java.util.Map;

import otocloud.framework.common.OtoCloudServiceLifeCycle;
import otocloud.framework.core.session.SessionStore;
import otocloud.persistence.dao.JdbcDataSource;

import com.hazelcast.config.Config;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.impl.Deployment;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public interface OtoCloudService extends OtoCloudServiceLifeCycle {
	EventBus getBus();
	
	Long getAppId();	
	Long getAppVersion();
	
	//void setServiceId(String srvId);
	String getServiceName();
	String getRealServiceName();
	void setServiceName(String srvName);
	
	JdbcDataSource getSysDatasource();
	
	SessionStore getSessionStore();
	
	Map<String, Deployment> getComponents();
	
	void init(JsonObject srvCfg, Vertx compContainer, Config clusterCfg, JsonObject vertxOptionsCfg, Future<Void> initFuture); //使用外部容器vertx
	void init(JsonObject srvCfg, Config clusterCfg, JsonObject vertxOptionsCfg, Future<Void> initFuture); //创建独立vertx	
	
	void configService();
	
	List<OtoCloudComponent> createServiceComponents();	
	
	void run(Future<Void> runFuture) throws Exception;	
	
	void close(Future<Void> stopFuture) throws Exception;
	
	void deployComponent(String serviceName, JsonObject compDeploymentDesc, JsonObject compConfig, Future<Void> depFuture);
	void deployCompAndUpdateConfig(JsonObject compDeploymentDesc, JsonObject compConfig, Future<Void> depFuture);
	
	void undeployComponent(String compName, Future<Void> undepFuture);	
	void undeployCompAndUpdateConfig(String compName, Future<Void> undepFuture);
	
	void beforeInit(Handler<AsyncResult<Void>> next);
	
	void afterInit(Future<Void> initFuture);

	void beforeRun(Handler<AsyncResult<Void>> next);
	
	void afterRun(Future<Void> runFuture);
	
	void beforeStop(Handler<AsyncResult<Void>> next);
	
	void afterStop(Future<Void> stopFuture);	
	
	List<OtoCloudEventHandlerRegistry> createHandlers();
	
	void registerRestAPIs(String compName, List<HandlerDescriptor> handlerDescs, Future<Void> regFuture);
	void unregisterRestAPIs(Future<Void> unregFuture);

	Logger getLogger();
	
	void saveComponentConfig(String component, JsonObject compCfg, Future<Void> saveFuture);
	//ClassLoader getComponentClassLoader(String compName);
	
    String buildEventAddress(String addressBase);
    
    JsonObject getSrvCfg();
}
