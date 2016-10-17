/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import java.util.List;

import com.hazelcast.config.Config;

import otocloud.framework.app.engine.AppServiceEngine;
import otocloud.framework.common.OtoCloudServiceLifeCycle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.Deployment;
import io.vertx.core.json.JsonObject;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月27日
 * @author lijing@yonyou.com
 */
public interface OtoCloudServiceContainer extends OtoCloudServiceLifeCycle {
	Vertx getVertx();
	Vertx getVertxOfApp(String serviceName);
	
	VertxInstancePool getVertxInstancePool();
	
	boolean needCluster();
	
	String getContainerName();
	
	Config getClusterConfig();
	
	JsonObject getVertxOptins();
	
	JsonObject getMavenConfig();
	
	List<Deployment> getDeployments();
	
	List<OtoCloudService> getSystemServices();
	
	List<AppServiceEngine> getAppServices();
	
	void init(Future<Void> initFuture);
	
	void run(Future<Void> runFuture);
	
	void stop(Future<Void> stopFuture);	
	
	void deployService(JsonObject deploymentDesc, JsonObject srvConfig, Future<Void> depFuture);
	
	void undeployService(String serviceName, Future<Void> undepFuture);
	
	void deployManagerComponent(JsonObject deploymentDesc, JsonObject srvConfig, Future<Void> depFuture);
	
	void undeployManageComponent(String serviceName, Future<Void> undepFuture);
	
/*	ClassLoader getServiceClassLoader(String serviceName);
	ClassLoader getComponentClassLoader(String serviceName, String compName);*/
}
