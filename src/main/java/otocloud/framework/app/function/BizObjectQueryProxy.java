/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.app.function;

import java.util.ArrayList;
import java.util.List;

import otocloud.framework.core.OtoCloudEventDescriptor;
import otocloud.framework.core.OtoCloudEventHandlerRegistry;



/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public class BizObjectQueryProxy extends AppActivityImpl {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OtoCloudEventHandlerRegistry> registerEventHandlers() {
		List<OtoCloudEventHandlerRegistry> retMap = new ArrayList<OtoCloudEventHandlerRegistry>();		
		
		PartnerBizObjectQueryHandler partnerBizObjectQueryHandler = new PartnerBizObjectQueryHandler(this);
		retMap.add(partnerBizObjectQueryHandler);
		
		BizObjectFindOneHandler bizObjectFindOneHandler = new BizObjectFindOneHandler(this);
		retMap.add(bizObjectFindOneHandler);

		
		return retMap;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "partner_bo_query_proxy";
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getBizObjectType() {
		// TODO Auto-generated method stub
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BizRoleDescriptor> exposeBizRolesDesc() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<OtoCloudEventDescriptor> exposeOutboundBizEventsDesc() {
		// TODO Auto-generated method stub
		return null;
	}    


}