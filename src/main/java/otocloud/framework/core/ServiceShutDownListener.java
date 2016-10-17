package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class ServiceShutDownListener implements Runnable {
	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private OtoCloudService service;

	public ServiceShutDownListener(
			OtoCloudService service) {
		this.service = service;
	}

	@Override
	public void run() {
		
		logger.info("service ShutDown!");
		
		Future<Void> stopFuture = Future.future();
		try{
			service.close(stopFuture);
			stopFuture.setHandler(initRet -> {
				if (stopFuture.succeeded()) {
				} else {
					Throwable err = initRet.cause();
					logger.error(err.getMessage(), err);				
				}
			});
		}catch(Exception ex){
			logger.error(ex.getMessage(), ex);	
		}
		

	}
}
