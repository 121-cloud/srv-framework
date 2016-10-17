/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.persistence;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年8月22日
 * @author lijing@yonyou.com
 */
public class DataPersistentPolicyFactory {
	
	private DataPersistentPolicyImpl dataPersistentPolicy;
	private DataShardingPersistentPolicyImpl dataShardingPersistentPolicy;
	private String policyName = "single";
	
	public DataPersistentPolicyFactory(){		
	}
	
	public DataPersistentPolicyFactory(String policyName){
		setPolicyName(policyName);
	}
	
	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public DataPersistentPolicy getPolicy(){
		switch (policyName) {
		case "sharding":
			if(dataShardingPersistentPolicy == null)
				dataShardingPersistentPolicy = new DataShardingPersistentPolicyImpl();
			return dataShardingPersistentPolicy;
		default:
			if(dataPersistentPolicy == null)
				dataPersistentPolicy = new DataPersistentPolicyImpl();
			return dataPersistentPolicy;
		}
	}
	

	
}
