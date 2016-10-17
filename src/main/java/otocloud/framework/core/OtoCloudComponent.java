/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.List;

import otocloud.common.OtoCloudLogger;
import otocloud.persistence.dao.JdbcDataSource;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月20日
 * @author lijing@yonyou.com
 */
public interface OtoCloudComponent extends Verticle {
	String getName();
	
	OtoCloudService getService();
	
	JdbcDataSource getSysDatasource();
	
	OtoCloudLogger getLogger();	
	
	EventBus getEventBus();	
	
	String buildEventAddress(String addressBase);
	
	String buildApiRegAddress(String addressBase);

	List<OtoCloudEventHandlerRegistry> registerEventHandlers();	
	
	void saveConfig(Future<Void> saveFuture);
	
	JsonObject getDependencies();

}
