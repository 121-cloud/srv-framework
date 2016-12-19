/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.persistence;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月22日
 * @author lijing@yonyou.com
 */
public class CDODataPersistentPolicyFactory {
	
	private CDODataPersistentPolicyImpl cdoDataPersistentPolicy;
	private CDODataShardingPersistentPolicyImpl cdoDataShardingPersistentPolicy;
	
	private String policyName = "single";
	
	public CDODataPersistentPolicyFactory(){		
	}
	
	public CDODataPersistentPolicyFactory(String policyName){
		setPolicyName(policyName);
	}
	
	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public CDODataPersistentPolicy getPolicy(){
		switch (policyName) {
		case "sharding":
			if(cdoDataShardingPersistentPolicy == null)
				cdoDataShardingPersistentPolicy = new CDODataShardingPersistentPolicyImpl();
			return cdoDataShardingPersistentPolicy;
		default:
			if(cdoDataPersistentPolicy == null)
				cdoDataPersistentPolicy = new CDODataPersistentPolicyImpl();
			return cdoDataPersistentPolicy;
		}
	}
	

	
}
