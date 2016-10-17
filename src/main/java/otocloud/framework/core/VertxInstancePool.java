/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.hazelcast.config.Config;
import otocloud.common.CommandCodec;
import otocloud.common.CommandResultCodec;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月14日
 * @author lijing@yonyou.com
 */
public class VertxInstancePool {
	
	private List<Vertx> appVertxPool;
	

	/**
	 * @return the appVertxPool
	 */
	public List<Vertx> getAppVertxPool() {
		return appVertxPool;
	}
	
	public void init(JsonObject srvCfg, Config clusterCfg, int appVertxPoolSize, JsonObject vertxOptions, Future<Void> initFuture){
		List<Vertx> vertxSet = new ArrayList<Vertx>();
		appVertxPool = Collections.synchronizedList(vertxSet);

		createVertx(srvCfg, clusterCfg, appVertxPoolSize, vertxOptions, initFuture);
	}
	
	public void init(Vertx vertx, Future<Void> initFuture){
		List<Vertx> vertxSet = new ArrayList<Vertx>();
		appVertxPool = Collections.synchronizedList(vertxSet);

		vertx.eventBus().registerCodec(new CommandCodec());
		vertx.eventBus().registerCodec(new CommandResultCodec());
		registerFactory(vertx);						
		appVertxPool.add(vertx);	
		
		initFuture.complete();
	}
	
/*	public void close(Future<Void> closeFuture){
		if(appVertxPool != null && appVertxPool.size() > 0){
			AtomicInteger closedCount = new AtomicInteger(0);
			int size = appVertxPool.size();
			for (int i = 0; i < size; i++){	
				Vertx vertxInst = appVertxPool.get(i);
				OtoCloudServiceImpl.unRegisterServiceFactory(vertxInst);
				vertxInst.close(completionHandler -> {	
					if(completionHandler.succeeded()){
						
					}else{
            			Throwable err = completionHandler.cause();
            			err.printStackTrace();        				
            			closeFuture.fail(err);
            			return;
					}
		       		if (closedCount.incrementAndGet() >= size) {
		       			closeFuture.complete();	
		            }					
				});		
				
			}
		}else{
			closeFuture.complete();
		}
	}*/
	
	public void close(Future<Void> closeFuture){
		if(appVertxPool != null && appVertxPool.size() > 0){
			int size = appVertxPool.size();
			innerClose(size, 0, closeFuture);
		}else{
			closeFuture.complete();
		}
	}
	
	private void innerClose(int size, int index, Future<Void> closeFuture){
		Vertx vertxInst = appVertxPool.get(index);	
		unRegisterFactory(vertxInst);
		vertxInst.close(completionHandler -> {	
			if(completionHandler.succeeded()){
				
			}else{
    			Throwable err = completionHandler.cause();
    			err.printStackTrace();
			}
			int nextIdx = index + 1;					
			if (nextIdx < size)
				innerClose(size, nextIdx, closeFuture);
			else if (nextIdx >= size) {
				appVertxPool.clear();
				closeFuture.complete();
			}
		});		
				

	}
	
	public void unRegisterFactory(Vertx vertx){
		OtoCloudServiceImpl.unRegisterComponentFactory(vertx);	
	}
	
	public void createVertx(JsonObject srvCfg, Config clusterCfg, int size, JsonObject vertxOptions, Future<Void> retFuture) {
		if(size > 0){
			AtomicInteger runningCount = new AtomicInteger(0);
			for (int i = 0; i < size; i++){
				
				VertxOptions options = OtoCloudServiceImpl.createVertxOptions(srvCfg, clusterCfg, vertxOptions);  

				// 创建群集Vertx运行时环境
				Vertx.clusteredVertx(options, res -> {
					// 运行时创建完后初始化
					if (res.succeeded()) {				
						Vertx newVertxInstance = res.result();	
						newVertxInstance.eventBus().registerCodec(new CommandCodec());
						newVertxInstance.eventBus().registerCodec(new CommandResultCodec());
						registerFactory(newVertxInstance);						
						appVertxPool.add(newVertxInstance);	
					} else {
		    	    }					
		       		if (runningCount.incrementAndGet() >= size) {
		       			retFuture.complete();	
		            }					
				});		
				
			}

		}else{
			retFuture.complete();
		}			
	}
	
	public void registerFactory(Vertx vertx){
		OtoCloudServiceImpl.registerComponentFactory(vertx);		
	}
	
	public Vertx getVertx(){
		//查找最小负荷vertx容器
		int size = appVertxPool.size();
		int minimumSize = 0;
		Vertx minimumSizeVertx = null;
		for (int i = 0; i < size; i++) {
			Vertx vertxInst = appVertxPool.get(i);
			if(minimumSize == 0){
				minimumSize = vertxInst.deploymentIDs().size();
				minimumSizeVertx = vertxInst;
			}else{
				int tempSize = vertxInst.deploymentIDs().size();
				if(tempSize < minimumSize){
					minimumSize = tempSize;
					minimumSizeVertx = vertxInst;
				}
			}			
		}		
		return minimumSizeVertx;
	}	

}
