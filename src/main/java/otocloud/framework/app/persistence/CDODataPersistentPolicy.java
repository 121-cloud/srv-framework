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
public interface CDODataPersistentPolicy {
	String getTableName(String fromAccount, String toAccount, String tableName);
	JsonObject getQueryConditionForMongo(String fromAccount, String toAccount, JsonObject query);
}
