/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.common;

import otocloud.common.OtoConfiguration;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
public class AppConfiguration extends OtoConfiguration {
	
	//应用配置中的键
	//public static final String APP_ID_KEY = "app_id";//应用配置中的APPID键
	public static final String APP_VERSION_ID_KEY = "version_id";//应用版本ID
	public static final String SERVICE_NAME_KEY = "service_name";	
	public static final String APP_DESC_KEY = "app_desc";//APP名称描述
	public static final String MASTER_ROLE = "master_role";//APP名称描述	
	public static final String APP_INST_SCOPE = "app_inst_scope";//应用实例范围	
	public static final String WEBSERVER_HOST = "run_webserver"; //是否作为web服务器HOST
	public static final String APPINST_CONTEXT = "app_context"; 
	
	public static final String INST_COMMON ="inst_common"; //应用实例公共配置
	public static final String INST_CF_KEY ="inst_config"; //账户应用实例配置键	
	
	public static final int APP_VERTX_NUMBER = 1; //应用Vertx实例的数量	
	public static final String APP_VERTX_NUMBER_KEY = "vertx_pool_size"; //应用Vertx实例的数量		

	
/*	public static final String ACCOUNT_SERVICE_CFG = "account_servcie";	
	public static final String ACCOUNT_SERVICE_HOST = "host";	
	public static final String ACCOUNT_SERVICE_POST = "port";	*/
	
	public static final String APP_DATASOURCE = "app_datasource";
	public static final String APP_DATASHARDING_POLICY = "data_sharding_policy";
	
}
