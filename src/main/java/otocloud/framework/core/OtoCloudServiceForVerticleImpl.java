package otocloud.framework.core;

import io.vertx.core.Context;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudServiceForVerticleImpl extends OtoCloudServiceImpl implements OtoCloudServiceForVerticle {
	
	protected Vertx vertx;
	protected Context context;
	
	protected OtoCloudServiceContainer container;	

	
	  @Override
	  public Vertx getVertx() {
	    return vertx;
	  }


	  @Override
	  public void init(Vertx vertx, Context context) {
	    this.vertx = vertx;
	    this.context = context;	    
	    
	    createHandlers();
	    
		ContextImpl contextImpl = (ContextImpl)this.context;
		DeploymentOptions deploymentOptions = contextImpl.getDeployment().deploymentOptions();
		
		OtoCloudServiceDepOptions srvDeploymentOptions = (OtoCloudServiceDepOptions)deploymentOptions;
		container = srvDeploymentOptions.getContainer();

	  }
		

	  public String deploymentID() {
	    return context.deploymentID();
	  }

	  public JsonObject config() {
	    return context.config();
	  }
	  
	  public OtoCloudServiceContainer getContainer(){
		  return container;
	  }

	  /**
	   * Get the arguments used when deploying the Vert.x process.
	   * @return the list of arguments
	   */
	  public List<String> processArgs() {
	    return context.processArgs();
	  }

	  @Override
	  public void start(Future<Void> startFuture) throws Exception {
		  try{
	        JsonObject cfg = config();	        
		    
	        Future<Void> initFuture = Future.future();
	        
	        JsonObject clusterCfg = null;
	        JsonObject vertxOptions = null;
	        if(container != null){
	        	clusterCfg = container.getClusterConfig();
	        	vertxOptions = container.getVertxOptins();
	        }
	        
	        init(cfg, vertx, clusterCfg, vertxOptions, initFuture);  
	        initFuture.setHandler(ret -> {
	    		if(ret.succeeded()){   
	    			try{
		    			Future<Void> runFuture = Future.future();
		    			//运行
		               	run(runFuture);
		               	runFuture.setHandler(runRet -> {
		            		if(runRet.succeeded()){   
		            			logger.info("Service:[" + this.getRealServiceName() + "] start!");
		            			startFuture.complete();
		            		}else{
		            			Throwable err = ret.cause();
		            			//System.err.println(getAppName() + "run failed" + err);
		            			startFuture.fail(err);
		            		}
		               	});	               	
	    			}catch(Throwable t){        			
	        			//System.err.println(getAppName() + " run failed" + t);
	        			startFuture.fail(t);
	        		}
	    		}else{
	    			Throwable err = ret.cause();
	    			//System.err.println(getAppName() + " initialize failed" + err);
	    			startFuture.fail(err);
	    		}    		
	    	}); 		
		  }catch(Exception e){
			  throw e;
		  }
		  
	  }


	  @Override
	  public void stop(Future<Void> stopFuture) throws Exception {
		  logger.info("Service:[" + this.getRealServiceName() + "] stop!");
	      close(stopFuture);	  
	  }

/*		@Override
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
			components.clear();        
			
			beforeStop(next->{
				if(next.succeeded()){      
					Future<Void> unregApiFuture = Future.future();
					unregisterRestAPIs(unregApiFuture);				
					unregApiFuture.setHandler(unregRet -> {
	            		if(unregRet.succeeded()){					
	            			closeCompletedHandle(stopFuture);
	            		}else{
	               			this.futureStatusRollback();
	            			Throwable err = unregRet.cause();
	            			err.printStackTrace(); 
	            			stopFuture.fail(err);            			
	            		}
					});
				}else{
					this.futureStatusRollback();
					stopFuture.fail(next.cause());
				}
			});
			
			
		}
*/

}
