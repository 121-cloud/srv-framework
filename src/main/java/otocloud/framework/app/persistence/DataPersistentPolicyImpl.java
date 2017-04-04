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
	public JsonObject getQueryConditionForMongo(String account, String bizUnit, JsonObject query){
		if(query == null || query.size() == 0){
			if(bizUnit == null || bizUnit.isEmpty()){
				return new JsonObject().put("account", account);
			}
			JsonObject retQuery = new JsonObject();
			return retQuery.put("$and", new JsonArray()
				.add(new JsonObject().put("account", account))
				.add(new JsonObject().put("biz_unit", bizUnit)));
		}
		JsonObject retQuery = new JsonObject();
		if(bizUnit == null || bizUnit.isEmpty()){
			retQuery.put("$and", new JsonArray()
											.add(new JsonObject().put("account", account))
											.add(query));
		}else{
			retQuery.put("$and", new JsonArray()
				.add(new JsonObject().put("account", account))
				.add(new JsonObject().put("biz_unit", bizUnit))
				.add(query));
		}

		return retQuery;
	}
	
}
