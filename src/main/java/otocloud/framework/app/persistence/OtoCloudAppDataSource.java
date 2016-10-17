/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.persistence;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import otocloud.persistence.dao.MongoDataSource;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月21日
 * @author lijing@yonyou.com
 */
public class OtoCloudAppDataSource extends MongoDataSource {
	
	public OtoCloudAppDataSource(Vertx vertx, JsonObject datasourceCfg,
			DataPersistentPolicy dataPersistentPolicy) {
		super();
		this.dataPersistentPolicy = dataPersistentPolicy;
		super.init(vertx, datasourceCfg);
	}
	
	public static final String MONGO_BIZ_ROOT = "biz_root";
	public static final String MONGO_BIZ_THREAD = "biz_thread";	
	
	//private DataPersistentPolicyFactory dataPersistentPolicyFactory;	
	private DataPersistentPolicy dataPersistentPolicy;
	
	
	public DataPersistentPolicy getDataPersistentPolicy() {
		return dataPersistentPolicy;
	}

	public String getDBTableName(String account, String tableName){
		return dataPersistentPolicy.getTableName(account, tableName);
	}	
	
	public String getBizRootTableName(String account){
		return getDBTableName(account, MONGO_BIZ_ROOT);
	}
	
	public String getBizThreadTableName(String account){
		return getDBTableName(account, MONGO_BIZ_THREAD);
	}
}
