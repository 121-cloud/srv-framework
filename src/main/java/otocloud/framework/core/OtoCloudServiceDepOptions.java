/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

//import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.DeploymentOptions;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
//@DataObject
public class OtoCloudServiceDepOptions extends DeploymentOptions {
	private OtoCloudServiceContainer container;
	private String mainClass = "";
	
	public String getMainClass() {
		return mainClass;
	}


	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}


	/**
	 * @return the container
	 */
	public OtoCloudServiceContainer getContainer() {
		return container;
	}


	/**
	 * Constructor.
	 *
	 * @param container
	 * @param clusterConfg
	 */
	public OtoCloudServiceDepOptions(OtoCloudServiceContainer container, String mainClass) {
		super();
		this.container = container;
		this.setMainClass(mainClass);
	}


	/**
	 * Constructor.
	 *
	 */
	public OtoCloudServiceDepOptions() {
		super();
		// TODO Auto-generated constructor stub
	}
	

}
