/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月23日
 * @author lijing@yonyou.com
 */
//业务状态所描述的业务根类型
public enum BizRootType{
	BIZ_FLOW(0),  //业务流程作为应用根
	BIZ_OBJECT(1); //业务对象作为应用根

	private int nCode ; 
	
	private BizRootType( int _nCode) { 
	    this . nCode = _nCode; 
	} 
	
	@Override 
	public String toString() { 
	    return String.valueOf (this.nCode ); 
	} 
}
