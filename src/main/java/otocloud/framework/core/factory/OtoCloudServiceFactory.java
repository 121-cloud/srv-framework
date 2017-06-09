/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.factory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import otocloud.common.OtoCloudDirectoryHelper;
import otocloud.framework.core.OtoCloudServiceDepOptions;
import otocloud.framework.core.OtoCloudServiceImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月27日
 * @author lijing@yonyou.com
 */
public class OtoCloudServiceFactory implements VerticleFactory {
	
	protected Vertx vertx;
	
	@Override
	public void init(Vertx vertx) {
		this.vertx = vertx;
	}

	  @Override
	  public boolean requiresResolve() {
	    return true;
	  }
	  
	  public static String getServiceName(String identifier){
		    int pos = identifier.lastIndexOf("::");		    
		    if (pos != -1) {		    	
		    	return identifier.substring(pos + 2);
		    }
		    return identifier;
	  }

	  @Override
	  public void resolve(String identifier, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {
		  
		    int pos = identifier.lastIndexOf("::");
		    String serviceName;
		    String fileName;

		    if (pos != -1) {
		    	fileName = VerticleFactory.removePrefix(identifier.substring(0, pos));
		    	serviceName = identifier.substring(pos + 2);
		    } else {
		    	fileName = VerticleFactory.removePrefix(identifier);
		    	serviceName = null;
		    }
		    
		    String jarPath = OtoCloudDirectoryHelper.getLibDirectory();		
		    
		    String[] temps = fileName.split(":");
		    
		    File deploymentFile = new File(jarPath, temps[1] + "-" + temps[2] + ".jar");		    
		    
	    	final List<String> dependencies = new ArrayList<String>();
	    	try{
		          JarFile jarFile = new JarFile(deploymentFile);
		          Manifest manifest = jarFile.getManifest();
		          if (manifest != null) {
		            String classPaths = (String)manifest.getMainAttributes().get(new Attributes.Name("Class-Path"));
		            
		            if(classPaths != null && !classPaths.isEmpty()){
		            	String[] classPathArray = classPaths.split(" ");
		            	for(String classPath: classPathArray){
		            		dependencies.add(classPath);
		            	}
		            }
		          }
		          jarFile.close();
	    	  }catch(Exception e){
	    		  e.printStackTrace();
	    	  } 
	    	  
	    	try{
	    	  
	          List<String> urls = new ArrayList<String>();
	          urls.add(deploymentFile.getAbsolutePath());
	          if(dependencies != null && dependencies.size() > 0){
	        	  urls.addAll(OtoCloudServiceImpl.buildClassPathToList(dependencies)); 
	          }
	          
	          deploymentOptions.setExtraClasspath(urls);
	          //deploymentOptions.setIsolationGroup("__vertx_maven_" + fileName);
	          deploymentOptions.setIsolationGroup(fileName);
	          //URLClassLoader urlc = new URLClassLoader(new URL[]{deploymentFile.toURI().toURL()}, classLoader);
	          
	          //resolution.complete();
	          
	          loadServiceConfig(serviceName, deploymentOptions, null, resolution);
	          
    	  }catch (Exception e) {
    	      resolution.fail(e);
    	  }

	  }
	  
	  public void loadServiceConfig(String serviceName, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {
		  OtoCloudServiceDepOptions opts = (OtoCloudServiceDepOptions)deploymentOptions; 	
			
	      String main = opts.getMainClass();
	      
	      List<String> isolatedClasses = new ArrayList<String>();
	      isolatedClasses.add(main);	      
	      deploymentOptions.setIsolatedClasses(isolatedClasses);
		  
		  resolution.complete(main);
	  }

	  
	  
/*	  public void loadServiceConfig(String serviceName, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {

		  //identifier = VerticleFactory.removePrefix(identifier);
	      String descriptorFile = serviceName + ".json";
	      
	      String cfgFilePath = System.getProperty("user.dir") + "/conf/" + descriptorFile;			
			
	      vertx.fileSystem().readFile(cfgFilePath, result -> {
	    	    if (result.succeeded()) {
	    	    	try{
		    	    	String fileContent = result.result().toString(); 	    	        
		    	    	JsonObject descriptor = new JsonObject(fileContent);	    	
	
		    	    	String main = descriptor.getString("main");
					    if (main == null) {
					        throw new IllegalArgumentException(descriptorFile + " does not contain a main field");
					    }
	
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
					      
					  }catch (Exception e) {
					      resolution.fail(e);
					  }
	    	    }else{
	    	    	resolution.fail(result.cause());
	    	    }
		  });

	  }
*/
	  
	  @Override
	  public String prefix() {
	    return "otocloud_local";
	  }

	  @Override
	  public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
	    throw new IllegalStateException("Shouldn't be called");
	  }


}
