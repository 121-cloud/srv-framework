/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

//import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
//@DataObject
public class OtoCloudCompDepOptions extends DeploymentOptions {
	private OtoCloudService service;

	/**
	 * Constructor.
	 *
	 */
	public OtoCloudCompDepOptions() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public OtoCloudCompDepOptions(OtoCloudService service) {
		super();
		this.service = service;
	}

	/**
	 * Constructor.
	 *
	 * @param other
	 */
	public OtoCloudCompDepOptions(DeploymentOptions other) {
		super(other);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Constructor.
	 *
	 * @param json
	 */
	public OtoCloudCompDepOptions(JsonObject json) {
		super(json);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the service
	 */
	public OtoCloudService getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(OtoCloudService service) {
		this.service = service;
	}
	
}
