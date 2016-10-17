/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.persistence;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月22日
 * @author lijing@yonyou.com
 */
public class DataPersistentPolicyImpl implements DataPersistentPolicy {
	
/*	private static DataPersistentPolicyImpl dataPersistentPolicy = new DataPersistentPolicyImpl();
	
	public static DataPersistentPolicyImpl instance(){
		return dataPersistentPolicy;
	}*/
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTableName(String account, String tableName) {
		return tableName;
	}
	
	@Override
	public JsonObject getQueryConditionForMongo(String account, JsonObject query){
		if(query == null || query.size() == 0){
			return new JsonObject().put("account", account);
		}
		JsonObject retQuery = new JsonObject();
		retQuery.put("$and", new JsonArray()
										.add(new JsonObject().put("account", account))
										.add(query));

		return retQuery;
	}
	
}
