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
public class CDODataPersistentPolicyImpl implements CDODataPersistentPolicy {
	
/*	private static DataPersistentPolicyImpl dataPersistentPolicy = new DataPersistentPolicyImpl();
	
	public static DataPersistentPolicyImpl instance(){
		return dataPersistentPolicy;
	}*/
	

	@Override
	public String getTableName(String fromAccount, String toAccount,
			String tableName) {
		// TODO Auto-generated method stub
		return tableName;
	}

	@Override
	public JsonObject getQueryConditionForMongo(String fromAccount,
			String toAccount, JsonObject query) {
		if(query == null || query.size() == 0){
			return new JsonObject().put("from_account", fromAccount)
					.put("to_account", toAccount);
		}
		JsonObject retQuery = new JsonObject();
		retQuery.put("$and", new JsonArray()
										.add(new JsonObject().put("from_account", fromAccount)
												.put("to_account", toAccount))
										.add(query));

		return retQuery;
	}
	
}
