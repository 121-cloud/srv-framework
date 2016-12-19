/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.persistence;

import io.vertx.core.json.JsonObject;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月22日
 * @author lijing@yonyou.com
 */
public class CDODataShardingPersistentPolicyImpl implements CDODataPersistentPolicy {
	
/*	private static DataShardingPersistentPolicyImpl dataPersistentPolicy = new DataShardingPersistentPolicyImpl();
	
	public static DataShardingPersistentPolicyImpl instance(){
		return dataPersistentPolicy;
	}*/


	@Override
	public String getTableName(String fromAccount, String toAccount,
			String tableName) {
		// TODO Auto-generated method stub
		return tableName + "_" + fromAccount + "_" + toAccount;
	}

	@Override
	public JsonObject getQueryConditionForMongo(String fromAccount,
			String toAccount, JsonObject query) {		
		return query;
	}
	
}
