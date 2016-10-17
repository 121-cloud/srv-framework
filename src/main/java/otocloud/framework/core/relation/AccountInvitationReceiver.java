/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.relation;


import otocloud.framework.core.message.MessageReceiver;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public interface AccountInvitationReceiver extends MessageReceiver {
	void accept(InvitationMessage message,
			Handler<AsyncResult<Boolean>> retHandler);
	void reject(InvitationMessage message,
			Handler<AsyncResult<Boolean>> retHandler);
}
