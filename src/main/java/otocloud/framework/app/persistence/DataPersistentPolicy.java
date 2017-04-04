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
public interface DataPersistentPolicy {
	String getTableName(String account, String tableName);
	JsonObject getQueryConditionForMongo(String account, String bizUnit, JsonObject query);
}
