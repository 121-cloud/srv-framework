package otocloud.framework.core;
import io.vertx.core.Verticle;


/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月24日
 * @author lijing@yonyou.com
 */
public interface OtoCloudServiceForVerticle extends OtoCloudService, Verticle {
	
	OtoCloudServiceContainer getContainer();	

}
