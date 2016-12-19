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
public class OtoCloudCDODataSource extends MongoDataSource {
	
	public OtoCloudCDODataSource(Vertx vertx, JsonObject datasourceCfg,
			CDODataPersistentPolicy dataPersistentPolicy) {
		super();
		this.dataPersistentPolicy = dataPersistentPolicy;
		super.init(vertx, datasourceCfg);
	}
	
	public static final String MONGO_BIZ_ROOT = "biz_root";
	public static final String MONGO_BIZ_THREAD = "biz_thread";	
	
	//private DataPersistentPolicyFactory dataPersistentPolicyFactory;	
	private CDODataPersistentPolicy dataPersistentPolicy;
	
	
	public CDODataPersistentPolicy getDataPersistentPolicy() {
		return dataPersistentPolicy;
	}

	public String getDBTableName(String fromAccount, String toAccount, String tableName){
		return dataPersistentPolicy.getTableName(fromAccount, toAccount, tableName);
	}	
	
	public String getBizRootTableName(String fromAccount, String toAccount, String account){
		return getDBTableName(fromAccount, toAccount, MONGO_BIZ_ROOT);
	}
	
	public String getBizThreadTableName(String fromAccount, String toAccount){
		return getDBTableName(fromAccount, toAccount, MONGO_BIZ_THREAD);
	}
}
