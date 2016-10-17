/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.common;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public enum OtoCloudServiceState {
	UNKNOWN(0),  //初始化前未知状态
	INITIALIZED(1), //初始化状态
	RUNNING(2), //运行状态
	STOPPED(3), //停止状态	
	AT_FAULT(4); //故障
	
    private int nCode ; 
    
    private OtoCloudServiceState( int _nCode) { 
        this.nCode = _nCode; 
    } 
    
    @Override 
    public String toString() { 
        return String.valueOf (this.nCode ); 
    } 
    
    
    public static String toDisplay(OtoCloudServiceState appState){
		String retString = appState.toString();
		switch (appState) {
		case UNKNOWN:
			retString = "运行时未知";
			break;
		case INITIALIZED:
			retString = "运行时初始化";
			break;
		case RUNNING:
			retString = "实例运行";
			break;
		case STOPPED:
			retString = "实例停止";
			break;
		case AT_FAULT:
			retString = "故障";
			break;
		default:
			break;
		}
		return retString;
	}
    
}
