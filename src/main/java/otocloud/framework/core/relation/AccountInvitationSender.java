/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.relation;

import otocloud.framework.core.message.MessageSender;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
public interface AccountInvitationSender extends MessageSender {
	/*{
		_id:<消息ID>，
		sponsor:{account,app,app_inst,from_role,to_role},
		invitee:{account,app,from_role,to_role},
		message:<邀请消息正文>,
		handled：<是否处理>
	}*/	
	//void invite(InvitationMessage invitationMessage, Handler<AsyncResult<String>> retHandler);
}
