/*
 * Copyright (C) 2015 121Cloud Project Group  All rights reserved.
 */
package otocloud.framework.core;

import java.util.List;

import otocloud.common.util.DateTimeUtil;
import otocloud.framework.common.IgnoreAuthVerify;
import otocloud.framework.common.IgnoreSession;
import otocloud.framework.core.session.Session;
import otocloud.framework.core.session.SessionStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年6月12日
 * @author lijing@yonyou.com
 */
public abstract class OtoCloudEventHandlerImpl<T> extends OtoCloudEventHandlerBase<T> {
	protected OtoCloudComponentImpl componentImpl;		
    
    public OtoCloudEventHandlerImpl(OtoCloudComponentImpl componentImpl) {
    	this.componentImpl = componentImpl;
    	
    	IgnoreAuthVerify ignoreAuthVerifyAnno = (IgnoreAuthVerify) getClass().getAnnotation(IgnoreAuthVerify.class);
    	if(ignoreAuthVerifyAnno != null){
    		this.ignoreAuthVerify = true;
    	}
    	
    	IgnoreSession ignoreSessionAnno = (IgnoreSession) getClass().getAnnotation(IgnoreSession.class);
    	if(ignoreSessionAnno != null){
    		this.ignoreSession = true;
    	}
    	
    }    
   
    @Override
    public String getRealAddress(){
    	String eventAddress = getEventAddress();
    	return componentImpl.buildEventAddress(eventAddress);
    }
    
    @Override
    public String getApiRegAddress(){
    	String eventAddress = getEventAddress();
    	return componentImpl.buildApiRegAddress(eventAddress);
    }
    
	@Override
	public void internalHandle(Message<JsonObject> msg){
		System.out.println("服务框架 收到请求消息！");
		
		CommandMessage<T> otoMsg = new CommandMessage<T>(msg, bus);	
		
		if(this.ignoreSession){
			toHandle(otoMsg);
			return;
		}
		
		//读取session信息
		sessionHandle(otoMsg, next->{
			if(next.succeeded()){
		    	if(this.ignoreAuthVerify){
		    		toHandle(otoMsg);
		    	}else{		
					JsonObject session = otoMsg.getSession();
					if(session != null){
						Future<Boolean> authFuture = Future.future();
						authFuture.setHandler(authRet->{
							if(authRet.succeeded()){
								if(authRet.result()){									
									toHandle(otoMsg);
								}else{
									otoMsg.fail(100, "此用户无访问权限.");
								}
							}else{
								Throwable err = authRet.cause();
								err.printStackTrace();
								otoMsg.fail(100, "权限验证失败." + err.getMessage());
							}					
						});
						//权限验证，构建callContxt
						authVerify(otoMsg, session, authFuture);
					}else{
						toHandle(otoMsg);
					}
		    	}
	    	
			}else{
				Throwable err = next.cause();
				//componentImpl.getLogger().error(err.getMessage(), err);
				otoMsg.fail(100, err.getMessage());
			}
		});
	}
	
	private void sessionHandle(CommandMessage<T> msg, Handler<AsyncResult<Void>> next){
		Future<Void> future = Future.future();
		future.setHandler(next);
		
		MultiMap headers = msg.headers();
		if(headers != null && headers.contains("token")){
			String token = headers.get("token");
			SessionStore sessionStore = this.componentImpl.getService().getSessionStore();
			if(sessionStore != null){
				sessionStore.get(token, sessionRet->{
					if(sessionRet.succeeded()){
						Session session = sessionRet.result();
						session.getAll(retHandler->{
							if(retHandler.succeeded()){
								msg.setSession(retHandler.result());
							}
							future.complete();
							session.close(closeHandler->{							
							});
						});	
					}else{
						Throwable err = sessionRet.cause();
						componentImpl.getLogger().error(err.getMessage(), err);
						future.fail(err);
					}
				});				
			}else{
				future.complete();
			}
		}else{
			future.complete();
		}
	}
	
	private void toHandle(CommandMessage<T> otoMsg){
		if(otoMsg.needAsyncReply()){
			otoMsg.reply("ok");
		}
		handle(otoMsg);
	}    
 
	protected void authVerify(CommandMessage<T> msg, JsonObject session, Future<Boolean> isOk){
    	
		Long userId = Long.parseLong(session.getString("user_id"));
		Long acctId = Long.parseLong(session.getString("acct_id"));
		if(userId > 0){
			if(acctId > 0){
				Long appId = componentImpl.getService().getAppId();		
				if(appId > 0){
					String componentName = componentImpl.getName();
					String querySql = "SELECT d_acct_biz_unit_id,d_org_role_id,d_is_global_bu,acct_biz_unit_post_id,d_app_activity_id FROM view_user_activity WHERE auth_user_id=? AND acct_id=? AND d_app_id=? AND d_app_activity_code=? limit 1";
					
					  try{
						  
						  JDBCClient sqlClient = componentImpl.getSysDatasource().getSqlClient();
						  
						  sqlClient.getConnection(connRes -> {
								if (connRes.succeeded()) {
									final SQLConnection conn = connRes.result();				
									conn.setAutoCommit(true, res ->{
									  if (res.failed()) {
										  closeDBConnect(conn);
						  	    		  Throwable err = res.cause();
						  	    		  String replyMsg = err.getMessage();
						  	    		  componentImpl.getLogger().error("权限验证出错：" + replyMsg, err);
						  	    		  isOk.complete(false);
									  }else{										
										conn.queryWithParams(querySql, new JsonArray()									
												.add(userId)
												.add(acctId)
												.add(appId)
												.add(componentName),
										  appSubRet->{	
											  try{
												  if (appSubRet.succeeded()) {
													  ResultSet result = appSubRet.result();
													  if(result != null){
														  List<JsonObject> retData = result.getRows();
														  if(retData.size() > 0){
															  JsonObject retItem = retData.get(0);
															  
															  //构建当前调用上下文
															  JsonObject callContext = msg.getCallContext();
															  callContext.put("biz_unit_id", retItem.getLong("d_acct_biz_unit_id"));
															  callContext.put("org_role_id", retItem.getLong("d_org_role_id"));															  
															  callContext.put("is_global_bu", retItem.getInteger("d_is_global_bu")==1?true:false);
															  callContext.put("biz_unit_post_id", retItem.getLong("acct_biz_unit_post_id"));
															  callContext.put("app_activity_id", retItem.getLong("d_app_activity_id"));			
															  callContext.put("date_time", DateTimeUtil.now("yyyy-MM-dd hh:mm:ss.SSS"));
															  
															  //加入调用链末端
															  msg.getCallChain().add(callContext.copy());
															  
															  isOk.complete(true);														  
															  return;
														  }
													  }
													  
													String apiDependonSql = "SELECT count(dependon_app_activity_id) as num FROM view_acct_app_activity_dependon WHERE acct_id=? AND dependon_app_activity_code=?";
													conn.queryWithParams(apiDependonSql, new JsonArray()									
															.add(acctId)
															.add(componentName),
														apiDependonRet->{	
															if (apiDependonRet.succeeded()) {
												            	ResultSet resultSet = apiDependonRet.result();
												            	List<JsonObject> retDataArrays = resultSet.getRows();
												            	if(retDataArrays != null && retDataArrays.size() > 0){
												            		Long num = retDataArrays.get(0).getLong("num");
												            		if(num > 0){
												            			isOk.complete(true);
												            			return;
												            		}									
												            	}
											            		componentImpl.getLogger().info("用户:" + userId + "无权限访问" + this.getRealAddress());
												            	isOk.complete(false);												            	
															}else{
												  	    		  Throwable err = apiDependonRet.cause();
												  	    		  String replyMsg = err.getMessage();
												  	    		  componentImpl.getLogger().error("权限验证出错：" + replyMsg, err);								  	    		  
												  	    		  isOk.complete(false);
															}
														}
													  );													  
													  
												  }else{
									  	    		  Throwable err = appSubRet.cause();
									  	    		  String replyMsg = err.getMessage();
									  	    		  componentImpl.getLogger().error("权限验证出错：" + replyMsg, err);	
									  	    		  isOk.complete(false);
												  }
												  
											  }finally{
												  closeDBConnect(conn);
											  }
										  });
									}
								});
				
							}else{
					    		  Throwable err = connRes.cause();
					    		  String replyMsg = err.getMessage();
					    		  componentImpl.getLogger().error("权限验证出错：" + replyMsg, err);
					    		  isOk.complete(false);
								}
							});
						  
					  }catch(Exception ex){
				  		  String replyMsg = ex.getMessage();
				  		  componentImpl.getLogger().error("权限验证出错：" + replyMsg, ex);
				  		  isOk.complete(false);
					  }
				}else{
					String errMsg = "应用 app_id 为空";
					isOk.complete(false);
					componentImpl.getLogger().error(errMsg);
				}	
			}else{
				String errMsg = "企业账户 acct_id 为空.";
				isOk.complete(false);
				componentImpl.getLogger().error(errMsg);
			}
		}else{
			String errMsg = "用户 user_id 为空.";
			isOk.complete(false);
			componentImpl.getLogger().error(errMsg);
		}		
	}
	
    
	private void closeDBConnect(SQLConnection conn){
		conn.close(handler->{
			if (handler.failed()) {
				Throwable conErr = handler.cause();
				componentImpl.getLogger().error(conErr.getMessage(), conErr);
			} else {								
			}
		});				
	}
    

    
}