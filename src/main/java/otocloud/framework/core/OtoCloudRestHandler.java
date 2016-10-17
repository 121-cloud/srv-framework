/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public interface OtoCloudRestHandler {  

    void register(Router restRouter);
	
    void handle(RoutingContext routingContext);    
}