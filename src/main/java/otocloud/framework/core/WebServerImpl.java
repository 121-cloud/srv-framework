/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.Deployment;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import otocloud.common.ActionURI;
import otocloud.common.OtoConfiguration;
import otocloud.common.util.RestResponseUtil;
import otocloud.framework.core.WebServer;
import otocloud.framework.core.ApiParameterDescriptor;
import otocloud.framework.core.OtoCloudComponent;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月26日
 * @author lijing@yonyou.com
 */
public class WebServerImpl implements WebServer {

	protected Vertx vertx;
	protected Router router;
	protected HttpServer server;
	protected BridgeOptions options;	
	protected String appId;
	protected JsonObject cfg;
	
	protected List<String> webEventAddresses;
	
	protected Map<String, RestActionDescriptor> actions;
	protected Map<Route, RestActionDescriptor> restRoutes;
	
	protected Logger logger;
	protected String srvName;
	
	public void init(String appId, String srvName, Vertx theVertx, 
			JsonObject webCfg, Logger thelogger){
		this.appId = appId;
		init(srvName, theVertx, webCfg, thelogger);
	}
	
	@Override
	public void init(String srvName, Vertx theVertx, 
			JsonObject webCfg, Logger thelogger) {		
		this.srvName = srvName;
		this.cfg = webCfg;
		logger = thelogger;
		vertx = theVertx;
		router = Router.router(vertx);	
	
		if(logger == null)
			configLogging();
	}
	
	private Boolean webEventbusIsEnabled(){
		if(cfg.containsKey(OtoConfiguration.EVENTBUS_ENABLED)){
			return cfg.getBoolean(OtoConfiguration.EVENTBUS_ENABLED);
		}
		return false;
	}
	
	private Boolean resServiceIsEnabled(){
		if(cfg.containsKey(OtoConfiguration.STATIC_RES_SERVICE)){
			return cfg.getBoolean(OtoConfiguration.STATIC_RES_SERVICE);
		}
		return false;
	}
	
	private void configLogging() {
		logger = LoggerFactory.getLogger(this.getClass().getName());
	}
	
	
	@Override
	public Map<String, RestActionDescriptor> getActionRoutes() {
		return actions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getOutboundEventAddresses() {
		return webEventAddresses;		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void restRoute(Map<String, Deployment> components) {		
		actions = new HashMap<String, RestActionDescriptor>();		
		Map<String, ActionURI> actionUrlsMap = actionUrlSetting();		
		getActionRoutes(components, actionUrlsMap);	
		
		router.route().handler(CorsHandler.create("*")
			      .allowedMethod(HttpMethod.GET)
			      .allowedMethod(HttpMethod.POST)
			      .allowedMethod(HttpMethod.PUT)
			      .allowedMethod(HttpMethod.DELETE)
			      .allowedMethod(HttpMethod.OPTIONS)
			      .allowedHeader("X-PINGARUNER")
			      .allowedHeader("Content-Type"));		
		
		router.route().handler(BodyHandler.create());	
		
		if(actions.size() > 0){
/*			BiConsumer<String, RestActionDescriptor> createRoute= (key,actionDesc) -> {
				
			};*/			
			restRoutes = new HashMap<Route,RestActionDescriptor>();
			actions.forEach((address, restActionDesc)  -> {		
				HttpMethod httpMethod = restActionDesc.getActionURI().getHttpMethod();
				//String apiUrl = restActionDesc.getActionURI().getUri();
				
				String restApiURI = restActionDesc.getActionURI().getUri();
				if(restApiURI.isEmpty()){
					restApiURI = "/" + appId + "/" + restActionDesc.getComponentName();
				}else{
					restApiURI = "/" + appId + "/" + restActionDesc.getComponentName() + "/" + restApiURI;
				}
				
				Route route = router.route(httpMethod, restApiURI);
				this.logger.info("注册 address:" + address + " -> url:" + restApiURI + ", http-method:" + httpMethod.toString());
				
				restRoutes.put(route, restActionDesc);
				route.handler(this::restRouteHandle);
			});
		}		
	}
	
	private void getActionRoutes(Map<String, Deployment> components, Map<String, ActionURI> actionUrlsMap){		
		components.forEach((key, comDep) -> {
				Set<Verticle> deployments = comDep.getVerticles();
				Iterator<Verticle> it = deployments.iterator();
				OtoCloudComponent component = (OtoCloudComponent)it.next();			
			
				List<OtoCloudEventHandlerRegistry> actionDescs = component.getEventHandlers();
				if(actionDescs != null && actionDescs.size() > 0){
					actionDescs.forEach(actionDesc -> {
						if(actionDesc.getHanlderDesc().getRestApiURI() != null){							
							RestActionDescriptor restActionDesc = new RestActionDescriptor(actionDesc.getHanlderDesc());	
							restActionDesc.setComponentName(component.getName());
							String addressString = restActionDesc.getActonDesc().getHandlerAddress().getEventAddress();
							if(actionUrlsMap != null && actionUrlsMap.containsKey(addressString)){
								restActionDesc.setActionURI(actionUrlsMap.get(addressString));
								actions.put(addressString, restActionDesc);
							}else{
								ActionURI actUrl = actionDesc.getHanlderDesc().getRestApiURI();
								restActionDesc.setActionURI(actUrl);
								actions.put(addressString, restActionDesc);							
							}
						}
					});
				}					
			});		
	}	

	
	private void restRouteHandle(RoutingContext routingContext){
		Route route = routingContext.currentRoute();
		RestActionDescriptor restActionDesc = restRoutes.get(route);
		
		HttpServerRequest request = routingContext.request();
		HttpServerResponse response = routingContext.response();

		//设置事件总线分发参数
		DeliveryOptions options = null;
		if(this.cfg != null && 
				this.cfg.containsKey(OtoConfiguration.EB_DELIVERY_OPTIONS)){
			options = new DeliveryOptions(this.cfg.getJsonObject(OtoConfiguration.EB_DELIVERY_OPTIONS));			
		}else{
			options = new DeliveryOptions();
		}		
		
		//固定添加事件消息头
		// contex中一定要有四个字段，格式：“{actor}|{access_token}”，如果没有值可以空着，如：“|1310233999”
		// TODO 如果context中没有四个参数直接返回，提示：传入参数不正确
		HandlerHttpContext actionCtxt = HandlerContextTransfomer.fromHttpRequestParamToMessageHeader(request);		
		HandlerContextTransfomer.setBusMessageHeader(actionCtxt, options);
		
		//------获取token进行访问验证-------
/*		String actorAccount = actionCtxt.getAccount();
		String actor = actionCtxt.getActor();
		String accessToken = actionCtxt.getAccessToken();*/
		
		//todo: 访问验证
		
		//----------------------------			

		
		HttpMethod method = restActionDesc.getActionURI().getHttpMethod();
		
		//构建参数
		List<ApiParameterDescriptor> paramNames = restActionDesc.getActonDesc().getParamsDesc();		
		if(paramNames != null && paramNames.size() > 0){
			buildEBHeader(options, request, method, paramNames);
		}
		
		//String targetAccount = actionCtxt.getTargetAccount();
		
		EventBus bus = vertx.eventBus();
		String address = restActionDesc.getActonDesc().getHandlerAddress().getEventAddress();		
		
		logger.info(formatLogMessage(String.format("REST请求分发：URI：%s，HttpMethod：%s，Address：%s", 
													restActionDesc.getActionURI().getUri(), method.toString(), address )));
		
		boolean needReply = restActionDesc.getActonDesc().getHandlerAddress().isNeedReply();			
		
		Object msg = null;
		
		switch (method) {
		case POST:
		case PUT:			
			msg = constructMessage(request, routingContext);
			bus.send(address, msg, options, reply -> {
	            if (reply.succeeded()) {
	            	if(!needReply){
	            		response.end();
	            	}else{
		            	Object retObject = reply.result().body();              
		            	response.putHeader("content-type", "application/json");
		            	if(retObject instanceof JsonObject){
		            		response.end(((JsonObject)retObject).encode());
		            	}else if(retObject instanceof JsonArray){
		            		response.end(((JsonArray)retObject).encode());
		            	}else{
		            		response.end(retObject.toString());
		            	}
	            	}
	            } else {
	            	Throwable err = reply.cause();
	            	String errMsg = "应用实例没启动，或消息处理错误： " + err.getMessage();
	            	RestResponseUtil.sendError(500, errMsg, response);
	            	writeWebLog(errMsg, err, request);
	            }
	        }); 
			
			break;
		case GET:		
			msg = new JsonObject();
			bus.send(address, msg, options, reply -> {
	            if (reply.succeeded()) {	            	
	            	if(!needReply){
	            		response.end();
	            	}else{
		            	Object retObject = reply.result().body();              
		            	response.putHeader("content-type", "application/json");
		            	if(retObject instanceof JsonObject){
		            		response.end(((JsonObject)retObject).encode());
		            	}else if(retObject instanceof JsonArray){
		            		response.end(((JsonArray)retObject).encode());
		            	}else{
		            		response.end(retObject.toString());
		            	}
	            	}
	            } else {
	            	Throwable err = reply.cause();
	            	String errMsg = "应用实例没启动，或消息处理错误： " + err.getMessage();
	            	RestResponseUtil.sendError(500, errMsg, response);
	            	writeWebLog(errMsg, err, request);	            }
	        }); 			
			
			break;
		case DELETE:	
			msg = constructMessage(request, routingContext);
			//msg = new JsonObject();
			bus.send(address, msg, options, reply -> {
	            if (reply.succeeded()) {
	            	if(!needReply){
	            		response.end();
	            	}else{
		            	Object retObject = reply.result().body();              
		            	response.putHeader("content-type", "application/json");
		            	if(retObject instanceof JsonObject){
		            		response.end(((JsonObject)retObject).encode());
		            	}else if(retObject instanceof JsonArray){
		            		response.end(((JsonArray)retObject).encode());
		            	}else{
		            		response.end(retObject.toString());
		            	}
	            	}
	            } else {
	            	Throwable err = reply.cause();
	            	String errMsg = "应用实例没启动，或消息处理错误： " + err.getMessage();
	            	RestResponseUtil.sendError(500, errMsg, response);
	            	writeWebLog(errMsg, err, request);	            }
	        }); 			
			break;
		default:
			break;
		}		
	}
	
	private Object constructMessage(HttpServerRequest request, RoutingContext routingContext){
		boolean isJsonArray = false;
		Object msg = null;
		try{
			String contentType = request.getHeader("content-type");
			if(contentType.indexOf("json-array") >= 0){
				isJsonArray = true;
			}
		}catch(Exception e){
			isJsonArray = false;
		}
		if(isJsonArray){
			msg = routingContext.getBodyAsJsonArray();
			//JsonArray(routingContext.getBodyAsString());
		}else{
			try{
				msg = routingContext.getBodyAsJson();
			}catch(Exception e){
				try{
					msg = routingContext.getBodyAsJsonArray();
				}catch(Exception ex){
					msg = routingContext.getBodyAsString();
				}
			}
		}
		return msg;
	}
	
	
	//构建Action参数
	private void buildEBHeader(DeliveryOptions options, HttpServerRequest request, HttpMethod method, List<ApiParameterDescriptor> paramNames){
		paramNames.forEach(restParamName -> {	
			String paramValue = null;
			try{
				//从URL参数获取
				paramValue = request.getParam(restParamName.getParameterName());
			}catch(Exception e){
				paramValue = null;
				//logger.error(formatLogMessage(e.getMessage()), e.getCause());
				writeWebLog(e.getMessage(), e.getCause(), request);
			}
			
			if(paramValue == null){
				if(method == HttpMethod.POST || method == HttpMethod.PUT){					
					try{
						//从表单参数获取
						paramValue = request.getFormAttribute(restParamName.getParameterName());
					}catch(Exception e){
						paramValue = null;
						//logger.error(formatLogMessage(e.getMessage()), e.getCause());
						writeWebLog(e.getMessage(), e.getCause(), request);
					}
					if(paramValue == null){
						try{
							//从HTTP-HEAD参数获取
							paramValue = request.getHeader(restParamName.getParameterName());
						}catch(Exception e){
							paramValue = null;
							//logger.error(formatLogMessage(e.getMessage()), e.getCause());
							writeWebLog(e.getMessage(), e.getCause(), request);
						}
					}
				}else{
					try{
						//从HTTP-HEAD参数获取
						paramValue = request.getHeader(restParamName.getParameterName());
					}catch(Exception e){
						paramValue = null;
						//logger.error(formatLogMessage(e.getMessage()), e.getCause());
						writeWebLog(e.getMessage(), e.getCause(), request);
					}
				}
			}
			
			options.addHeader(restParamName.getParameterName(), paramValue);			
		});
	}
	

	
	@Override
	public void busRoute(OtoCloudService appInstance) {	
		if(!webEventbusIsEnabled())
			return;
		
		webEventAddresses = new ArrayList<String>();
		getOutboundEventAddress(appInstance); 
		
		if(webEventAddresses.size() > 0){
			options = new BridgeOptions();
			webEventAddresses.forEach(busAddress -> {				
				PermittedOptions outbound = new PermittedOptions().setAddress(busAddress);
				options.addOutboundPermitted(outbound);
			});

			SockJSHandler sockJSHandler = SockJSHandler.create(vertx);			
			sockJSHandler.bridge(options, event -> {

			      // You can also optionally provide a handler like this which will be passed any events that occur on the bridge
			      // You can use this for monitoring or logging, or to change the raw messages in-flight.
			      // It can also be used for fine grained access control.

/*				  BridgeEvent.Type socketEventType = event.type();
			      if (socketEventType == BridgeEvent.Type.SOCKET_CREATED) {
			    	  System.out.println("A socket was created");
			      }else if(socketEventType == BridgeEvent.Type.SOCKET_CLOSED){
			    	  System.out.println("A socket was closed");
			      }*/

			      // This signals that it's ok to process the event
			      event.complete(true);

			    });

			router.route("/eventbus/*").handler(sockJSHandler);
		}
	}
	
	
	private void getOutboundEventAddress(OtoCloudService appInst){		
		
			Map<String, Deployment> components = appInst.getComponents();
			components.forEach((key,deployment) -> {

				Set<Verticle> deployments = deployment.getVerticles();
				Iterator<Verticle> it = deployments.iterator();
				OtoCloudComponent component = (OtoCloudComponent)it.next();			
				
				//if(component instanceof OtoCloudComponent){
					OtoCloudComponent activity = (OtoCloudComponent)component;			
					//String account = activity.getAppInstContext().getAccount();
					//ActivityDescriptor activityDesc = activity.getActivityDescriptor();
					List<OtoCloudEventHandlerRegistry> eventsDesc = activity.getEventHandlers();
					if(eventsDesc != null && eventsDesc.size() > 0){
						eventsDesc.forEach(eventDesc -> {
							if(eventDesc.getHanlderDesc().getHandlerAddress().isSendToWeb()){
								/*String outboudAddress = buildAddress(eventDesc.getEventAddress(), 
										account);	*/						
								webEventAddresses.add(eventDesc.getEventAddress());							
							}
						});
					}
				//}
				
			});	

		
	}	
	
	
	@Override
	public void addOutboundEvent(OtoCloudService appInst){
		Map<String, Deployment> components = appInst.getComponents();
		components.forEach((key,deployment) -> {

			Set<Verticle> deployments = deployment.getVerticles();
			Iterator<Verticle> it = deployments.iterator();
			OtoCloudComponent component = (OtoCloudComponent)it.next();			
			
			//if(component instanceof OtoCloudComponent){
			OtoCloudComponent activity = (OtoCloudComponent)component;		
				//String account = activity.getAppInstContext().getAccount();
			//OtoCloudEventHandlerRegistry activityDesc = activity.getEventHandlers();
				List<OtoCloudEventHandlerRegistry> eventsDesc = activity.getEventHandlers();
				if(eventsDesc != null && eventsDesc.size() > 0){
					eventsDesc.forEach(eventDesc -> {
						if(eventDesc.getHanlderDesc().getHandlerAddress().isSendToWeb()){
							String outboudAddress = eventDesc.getEventAddress();//buildAddress(eventDesc.getEventAddress(), 
									 //account);
							if(!webEventAddresses.contains(outboudAddress)){
								webEventAddresses.add(outboudAddress);							
								PermittedOptions outbound = new PermittedOptions().setAddress(outboudAddress);
								options.addOutboundPermitted(outbound);		
							}
						}
					});
				}
			//}
			
		});	
	}
	
	@Override
	public void removeOutboundEvent(OtoCloudService appInst){
		Map<String, Deployment> components = appInst.getComponents();
		components.forEach((key,deployment) -> {

			Set<Verticle> deployments = deployment.getVerticles();
			Iterator<Verticle> it = deployments.iterator();
			OtoCloudComponent component = (OtoCloudComponent)it.next();			

			//if(component instanceof AppActivity){
			OtoCloudComponent activity = (OtoCloudComponent)component;		
				//String account = activity.getAppInstContext().getAccount();
				//ActivityDescriptor activityDesc = activity.getActivityDescriptor();
			List<OtoCloudEventHandlerRegistry> eventsDesc = activity.getEventHandlers();
				if(eventsDesc != null && eventsDesc.size() > 0){
					eventsDesc.forEach(eventDesc -> {
						if(eventDesc.getHanlderDesc().getHandlerAddress().isSendToWeb()){
							String outboudAddress = eventDesc.getEventAddress(); //buildAddress(eventDesc.getEventAddress(), 
									 //account);
							
							webEventAddresses.remove(outboudAddress);	
							
							List<PermittedOptions> outboundLstList = options.getOutboundPermitteds();
							outboundLstList.stream().filter(e -> e.getAddress().equals(outboudAddress)).forEach(outbound -> {
								outboundLstList.remove(outbound);
							});
							
						}
					});
				}	
			//}
			
		});	
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void listen() {		
		if(resServiceIsEnabled()){
		    // Serve the static resources
		    router.route().handler(StaticHandler.create());	
		}
		
		server = vertx.createHttpServer();		
		
		int lstPort = cfg.getInteger(OtoConfiguration.WEBSERVER_PORT);
		server.requestHandler(router::accept).listen(lstPort);
		
		logger.info("内部 webserver启动,监听端口:" + lstPort);
		
	}

	@Override
	public void close() {		
		server.close();
	}
	

	private String formatLogMessage(String msg) {
		String retMsg = "";
		if(msg != null && !msg.isEmpty())
			retMsg = msg;
		if(srvName == null || srvName.isEmpty())
			return retMsg;
		return "[" + srvName + "]:" + retMsg;		
	}
	
	private String formatLogMessage(String msg, HttpServerRequest req) {
		String retMsg = "";
		String logPrefix = String.format("Client:%s:%s ",
				req.remoteAddress().host(), String.valueOf(req.remoteAddress().port()));
		
		if(msg != null && !msg.isEmpty())
			retMsg = msg;
		if(srvName == null || srvName.isEmpty())
			return logPrefix +  retMsg;
		return logPrefix + "[" + srvName + "]:" + retMsg;		
	}
	

	private void writeWebLog(String msg, Throwable err, HttpServerRequest req){
		logger.error(formatLogMessage(msg, req), err.getCause());
	}

	@Override
	public Map<String, ActionURI> actionUrlSetting() {
		Map<String, ActionURI> retMap = new HashMap<String, ActionURI>();		

		return retMap;
	}
	
	

}
