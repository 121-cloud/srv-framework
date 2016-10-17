/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.common;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public interface OtoCloudServiceLifeCycle {
	//服务当前状态
	OtoCloudServiceState getCurrentStatus();
	void setCurrentStatus(OtoCloudServiceState newState);
	//状态变化通知
	void statusChangedNotify(OtoCloudServiceState oldState, OtoCloudServiceState newState);
	
	//服务未来状态
	void setFutureStatus(OtoCloudServiceState theFutureState); //设置预期状态
	OtoCloudServiceState getFutureStatus();
	void futureStatusComplete(); //预期状态转成完成状态
	void futureStatusRollback(); //预期状态回滚
	Boolean stateChangeIsReady(); //判断状态变化是否就绪
	
/*	//可以多次执行，变stopped状态为running状态
	void run(Future<Void> runFuture) throws Exception;
	
	//可以多次执行，变running状态为stopped状态
	void stop(Future<Void> stopFuture) throws Exception;*/
}
