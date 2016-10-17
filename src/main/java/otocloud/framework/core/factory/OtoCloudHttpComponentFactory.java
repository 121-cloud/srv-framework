package otocloud.framework.core.factory;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月27日
 * @author lijing@yonyou.com
 */
public class OtoCloudHttpComponentFactory extends OtoCloudHttpServiceFactory {

  @Override
  public String prefix() {
    return "component_http";
  }
  
  @Override
  public String internalPrefix() {
	  String preFixStr = prefix();
	  if(preFixStr.equals("component_http"))
	    return "http";
	  return "https";
  }

  @Override
  public void internalDeploy(String serviceName, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {
	  
	  JsonObject cfgNode = deploymentOptions.getConfig();
	  JsonObject parentServiceConfig = cfgNode.getJsonObject("serviceConfig");

      JsonObject descriptor = OtoCloudComponentFactory.getComponentConfig(parentServiceConfig, serviceName);
      
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
  
}

