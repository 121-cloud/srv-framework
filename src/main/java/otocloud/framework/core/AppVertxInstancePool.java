/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import otocloud.framework.core.factory.OtoCloudComponentFactory;
import otocloud.framework.core.factory.OtoCloudHttpComponentFactory;
import otocloud.framework.core.factory.OtoCloudHttpSecureComponentFactory;
import otocloud.framework.core.factory.OtoCloudHttpSecureServiceFactory;
import otocloud.framework.core.factory.OtoCloudHttpServiceFactory;
import otocloud.framework.core.factory.OtoCloudMavenComponentFactory;
import otocloud.framework.core.factory.OtoCloudMavenServiceFactory;
import otocloud.framework.core.factory.OtoCloudServiceFactory;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月14日
 * @author lijing@yonyou.com
 */
public class AppVertxInstancePool extends VertxInstancePool {
	
	@Override
	public void registerFactory(Vertx vertx){
		OtoCloudServiceFactory serviceFactory = new OtoCloudServiceFactory();	
		vertx.registerVerticleFactory(serviceFactory);
		
		OtoCloudMavenServiceFactory mavenServiceFactory = new OtoCloudMavenServiceFactory();
		vertx.registerVerticleFactory(mavenServiceFactory);
		
		OtoCloudHttpServiceFactory httpServiceFactory = new OtoCloudHttpServiceFactory();	
		vertx.registerVerticleFactory(httpServiceFactory);
		
		OtoCloudHttpSecureServiceFactory httpSecureServiceFactory = new OtoCloudHttpSecureServiceFactory();	
		vertx.registerVerticleFactory(httpSecureServiceFactory);
		
		OtoCloudComponentFactory componentFactory = new OtoCloudComponentFactory();	
		vertx.registerVerticleFactory(componentFactory);	
		
		OtoCloudMavenComponentFactory mavenComponentFactory = new OtoCloudMavenComponentFactory();
		vertx.registerVerticleFactory(mavenComponentFactory);
		
		OtoCloudHttpComponentFactory httpComponentFactory = new OtoCloudHttpComponentFactory();	
		vertx.registerVerticleFactory(httpComponentFactory);		
		
		OtoCloudHttpSecureComponentFactory httpSecureComponentFactory = new OtoCloudHttpSecureComponentFactory();	
		vertx.registerVerticleFactory(httpSecureComponentFactory);
	}
	
	@Override
	public void unRegisterFactory(Vertx vertx){
		Set<VerticleFactory> verticleFactories = vertx.verticleFactories();
		if(verticleFactories != null && verticleFactories.size() > 0){
			for(VerticleFactory verticleFactory : verticleFactories){
				vertx.unregisterVerticleFactory(verticleFactory);
			}			
		}
	}

}
