/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.common;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public class OtoCloudServiceLifeCycleImpl implements OtoCloudServiceLifeCycle {
	protected OtoCloudServiceState currentState = OtoCloudServiceState.UNKNOWN;
	protected OtoCloudServiceState futureState = OtoCloudServiceState.UNKNOWN;
	protected Boolean stateChangeIsReady = true; //状态变化是否就绪，如果没有，表示状态变化还未完成，不允许进行其它状态变化
	
	public void statusReset() {
		currentState = OtoCloudServiceState.UNKNOWN;
		futureState = OtoCloudServiceState.UNKNOWN;
		stateChangeIsReady = true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OtoCloudServiceState getCurrentStatus() {		
		return currentState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrentStatus(OtoCloudServiceState newState) {
		if(currentState != newState) {
			OtoCloudServiceState oldState = currentState;
			currentState = newState;			
			//trigger: 触发状态变化事件
			statusChangedNotify(oldState, newState);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFutureStatus(OtoCloudServiceState theFutureState) {
		stateChangeIsReady = false;
		futureState = theFutureState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public OtoCloudServiceState getFutureStatus() {
		return futureState;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void futureStatusComplete() {
		stateChangeIsReady = true;
		setCurrentStatus(futureState);
		futureState = OtoCloudServiceState.UNKNOWN;
	}	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void futureStatusRollback(){
		futureState = OtoCloudServiceState.UNKNOWN;
		stateChangeIsReady = true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean stateChangeIsReady(){
		return stateChangeIsReady;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void statusChangedNotify(OtoCloudServiceState oldState,
			OtoCloudServiceState newState) {
		// TODO Auto-generated method stub
		
	}

}
