/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.message;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public interface MessageSender {
	/*{
		_id:<消息ID>，
		sender:{account,app,app_inst,from_role,to_role},
		receiver:{account,app,from_role,to_role},
		message:<邀请消息正文>,
		msg_status：<消息状态>
	}*/	
	void send(OtoCloudMessage message, Handler<AsyncResult<String>> retHandler);
}
