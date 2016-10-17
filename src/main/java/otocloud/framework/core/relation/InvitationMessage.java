/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core.relation;

import otocloud.framework.core.message.MessageActor;
import otocloud.framework.core.message.OtoCloudMessage;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月6日
 * @author lijing@yonyou.com
 */
/*{
	id:<消息ID>，
	sponsor:{account,app,app_inst,from_role,to_role},
	invitee:{account,app,from_role,to_role},
	message:<邀请消息正文>,
	msgStatus：<消息状态>
}*/	
public class InvitationMessage extends OtoCloudMessage {
	
	public InvitationMessage(){		
		super();
	}

	public InvitationMessage(String id, Sponsor sponsor, Invitee invitee, String message,
			Integer msgStatus) {
		super(id, sponsor, invitee, message, msgStatus);
	}
	
	public InvitationMessage(Sponsor sponsor, Invitee invitee, String message,
			Integer msgStatus) {
		super(sponsor, invitee, message, msgStatus);
	}
	
	public InvitationMessage(Sponsor sponsor, Invitee invitee, String message){
		super(sponsor, invitee, message);
	}

	
	/**
	 * @return the sponsor
	 */
	public Sponsor getSponsor() {
		return (Sponsor)sender;
	}
	/**
	 * @param sponsor the sponsor to set
	 */
	public void setSponsor(Sponsor sponsor) {
		sender = sponsor;
	}
	/**
	 * @return the invitee
	 */
	public Invitee getInvitee() {
		return (Invitee)receiver;
	}
	/**
	 * @param invitee the invitee to set
	 */
	public void setInvitee(Invitee invitee) {
		this.receiver = invitee;
	}
	
	@Override
	public MessageActor createSender(){
		return new Sponsor();
	}
	
	@Override
	public MessageActor createReceiver(){
		return new Invitee();
	}
	
}
