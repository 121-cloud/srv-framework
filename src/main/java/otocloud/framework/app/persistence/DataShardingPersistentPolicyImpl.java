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
public class DataShardingPersistentPolicyImpl implements DataPersistentPolicy {
	
/*	private static DataShardingPersistentPolicyImpl dataPersistentPolicy = new DataShardingPersistentPolicyImpl();
	
	public static DataShardingPersistentPolicyImpl instance(){
		return dataPersistentPolicy;
	}*/
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTableName(String account, String tableName) {
		// TODO Auto-generated method stub
		return tableName + "_" + account;
	}
	
	@Override
	public JsonObject getQueryConditionForMongo(String account, String bizUnit, JsonObject query){
		if(query == null)
			return new JsonObject();
		return query;
	}
	
}
