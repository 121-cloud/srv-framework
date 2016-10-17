/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import otocloud.common.OtoConfiguration;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月21日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudWebServerCompImpl extends OtoCloudComponentImpl implements OtoCloudRestComponent {

	protected HttpServer server;
	protected Router router;
	protected BridgeOptions bridgeOptions;
	protected JsonObject webServerCfg;

	protected Map<String,PermittedOptions> outboundAddresses;  //缓存注册的应用实例状态变化地址
	
	@Override 
    public void start(Future<Void> startFuture) throws Exception {
		outboundAddresses = new HashMap<String, PermittedOptions>();
		webServerCfg = config().getJsonObject(OtoConfiguration.WEBSERVER_CFG);
		
		Future<Void> innerFuture = Future.future();
		super.start(innerFuture);
		innerFuture.setHandler(ret->{
			if(ret.succeeded()){
				
				router = Router.router(vertx);
				
				restRoute();
				
				if(webEventbusIsEnabled())
					busRoute();	
				
				if(resServiceIsEnabled()){
				    // Serve the static resources
				    router.route().handler(StaticHandler.create());	
				}
			    
			    listen();
			    startFuture.complete();
				
			}else{
               	Throwable err = ret.cause();
            	logger.error(err.getMessage(), err);
            	startFuture.fail(err);
			}
		});		

	}
	
	private Boolean webEventbusIsEnabled(){
		if(webServerCfg.containsKey(OtoConfiguration.EVENTBUS_ENABLED)){
			return webServerCfg.getBoolean(OtoConfiguration.EVENTBUS_ENABLED);
		}
		return false;
	}
	
	private Boolean resServiceIsEnabled(){
		if(webServerCfg.containsKey(OtoConfiguration.STATIC_RES_SERVICE)){
			return webServerCfg.getBoolean(OtoConfiguration.STATIC_RES_SERVICE);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restRoute() {
	
		router.route().handler(CorsHandler.create("*")
			      .allowedMethod(HttpMethod.GET)
			      .allowedMethod(HttpMethod.POST)
			      .allowedMethod(HttpMethod.DELETE)
			      .allowedMethod(HttpMethod.PUT)
			      .allowedMethod(HttpMethod.OPTIONS)
			      .allowedHeader("X-PINGARUNER")
			      .allowedHeader("Content-Type"));
		
		
		router.route().handler(BodyHandler.create());
		
		beforRoute(router);
		
		List<OtoCloudRestHandler> restRouteHandlers = getRestHandlers();
		if(restRouteHandlers != null && restRouteHandlers.size() > 0){
			restRouteHandlers.forEach(restRouteHandler ->{
				restRouteHandler.register(router);				
			});
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void busRoute() {
		//注册发布到WEB上的事件
		bridgeOptions = new BridgeOptions();
		
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx);			
		sockJSHandler.bridge(bridgeOptions, event -> {

		      // You can also optionally provide a handler like this which will be passed any events that occur on the bridge
		      // You can use this for monitoring or logging, or to change the raw messages in-flight.
		      // It can also be used for fine grained access control.

/*			  BridgeEvent.Type socketEventType = event.type();
		      if (socketEventType == BridgeEvent.Type.SOCKET_CREATED) {
		    	  System.out.println("A socket was created");
		      }else if(socketEventType == BridgeEvent.Type.SOCKET_CLOSED){
		    	  System.out.println("A socket was closed");
		      }
*/
		      // This signals that it's ok to process the event
		      event.complete(true);

		    });
		router.route("/eventbus/*").handler(sockJSHandler);
		
		
		List<String> outboundEventAddresses = getOutboundEventAddresses();
		if(outboundEventAddresses != null && outboundEventAddresses.size() > 0){
			outboundEventAddresses.forEach(busAddress -> {		
	    		if(!outboundAddresses.containsKey(busAddress)){    		
					PermittedOptions outbound = new PermittedOptions().setAddress(busAddress);
					bridgeOptions.addOutboundPermitted(outbound);				
					outboundAddresses.put(busAddress, outbound);	
	    		}    		

			});
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void listen() {
		server = vertx.createHttpServer();
		server.requestHandler(router::accept).listen(webServerCfg.getInteger(OtoConfiguration.WEBSERVER_PORT));	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		server.close();		
		outboundAddresses.clear();
	}	
	
	@Override
	public void stop(Future<Void> stopFuture) throws Exception{
			Future<Void> unregFuture = Future.future();
			super.stop(unregFuture);			
			unregFuture.setHandler(unregRet -> {
	    		if(unregRet.succeeded()){   
	    			close();
	    			stopFuture.complete();
	    		}else{
	    			Throwable err = unregRet.cause();
	    			err.printStackTrace();    
	    			stopFuture.fail(err);
	    		}    		
	       	});	  

	}
	
	/**
	 * @return the bridgeOptions
	 */
	public BridgeOptions getBridgeOptions() {
		return bridgeOptions;
	}
	
}
