/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.factory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import otocloud.common.OtoConfiguration;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月27日
 * @author lijing@yonyou.com
 */
public class OtoCloudComponentFactory extends OtoCloudServiceFactory {
 
	 public OtoCloudComponentFactory(){
	 }
	 
	 @Override
	 public void loadServiceConfig(String serviceName, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {
		  JsonObject cfgNode = deploymentOptions.getConfig();
		  JsonObject parentServiceConfig = cfgNode.getJsonObject("serviceConfig");

	      JsonObject descriptor = getComponentConfig(parentServiceConfig, serviceName);
	      
	      if(descriptor == null){
	    	  throw new IllegalArgumentException(serviceName + " does not contain a component config");
	      }

	      String main = descriptor.getString("main");
	      if (main == null) {
	        throw new IllegalArgumentException(serviceName + " does not contain a main field");
	      }
	      
	      cfgNode.remove("serviceConfig");

	      // Any options specified in the service config will override anything specified at deployment time
	      // With the exception of config which can be overridden with config provided at deployment time
	      JsonObject depOptions = deploymentOptions.toJson();
	      JsonObject depConfig = depOptions.getJsonObject("config", new JsonObject());
	      JsonObject serviceOptions = descriptor.getJsonObject("options", new JsonObject());
	      JsonObject serviceConfig = serviceOptions.getJsonObject("config", new JsonObject());
	      depOptions.mergeIn(serviceOptions);
	      serviceConfig.mergeIn(depConfig);
	      depOptions.put("config", serviceConfig);
	      deploymentOptions.fromJson(depOptions);
	      resolution.complete(main);
	  }

	  @Override
	  public String prefix() {
	    return "component_local";
	  }


		// 获取组件配置
		public static JsonObject getComponentConfig(JsonObject parentServiceConfig, String compName) {		
			JsonObject compCfg = null;
			JsonObject compOpCfg = null;
			if(!parentServiceConfig.containsKey(OtoConfiguration.COMPONENT_CFG)){
				compCfg = new JsonObject();
				compOpCfg = new JsonObject();
				compCfg.put("options", new JsonObject().put("config", compOpCfg));
				
				JsonObject compCateCfg = new JsonObject();
				compCateCfg.put(compName, compCfg);
				
				parentServiceConfig.put(OtoConfiguration.COMPONENT_CFG, compCateCfg);
			}else{
				JsonObject compCateCfg = parentServiceConfig.getJsonObject(OtoConfiguration.COMPONENT_CFG);				
				if(compCateCfg.containsKey(compName)){
					compCfg = compCateCfg.getJsonObject(compName);
					if(compCfg.containsKey("options")){
						JsonObject opCfg = compCfg.getJsonObject("options");
						if(opCfg.containsKey("config")){
							compOpCfg = opCfg.getJsonObject("config");
						}else{
							compOpCfg = new JsonObject();
							opCfg.put("config", compOpCfg);
						}						
					}else{
						compOpCfg = new JsonObject();
						compCfg.put("options", new JsonObject().put("config", compOpCfg));					
					}					
				}else{
					compCfg = new JsonObject();
					compOpCfg = new JsonObject();
					compCfg.put("options", new JsonObject().put("config", compOpCfg));
					compCateCfg.put(compName, compCfg);
				}				
			}
	
			//将公共配置注入应用实例配置
			reBuildCommonCfg(parentServiceConfig, compOpCfg);
			return compCfg;
			
		}
		
		public static void reBuildCommonCfg(JsonObject parentServiceConfig, JsonObject compConfig){
			if(!parentServiceConfig.containsKey(OtoConfiguration.COMPONENT_COMMON))
				return;
			
			//将公共配置注入应用实例配置
			JsonObject instCommonCfg = parentServiceConfig.getJsonObject(OtoConfiguration.COMPONENT_COMMON);
			instCommonCfg.forEach(item -> {
				String keyString = item.getKey();
				//实例配置可重写公共配置
				if(!compConfig.containsKey(keyString)){
					compConfig.put(keyString, item.getValue());	
				}			
			});
		}		


}
