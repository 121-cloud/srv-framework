package otocloud.framework.app.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;

public class PagingOptions {
	public PagingOptions(JsonObject queryCond, FindOptions findOptions) {
		this.queryCond = queryCond;
		this.findOptions = findOptions;
	}

	public JsonObject queryCond;
	public FindOptions findOptions;

	
	public static PagingOptions buildPagingOptions(JsonObject fields, JsonObject paging, JsonObject otherCond){
		if(paging == null)
			return null;
		
		JsonObject queryCond = otherCond;
		if(queryCond == null){
			queryCond = new JsonObject();
		}
		
		String sortField = paging.getString("sort_field");
		Integer sortDirection = paging.getInteger("sort_direction");
		Integer pageSize = paging.getInteger("page_size");
		
		FindOptions findOptions = new FindOptions();	
		JsonObject sortBson = new JsonObject().put(sortField, sortDirection);
		findOptions.setSort(sortBson);
		findOptions.setLimit(pageSize);
		
		if(fields != null){
			findOptions.setFields(fields);
		}
		
		JsonObject findQuery = null;
		int pageNumber = -1;
		if(paging.containsKey("page_number")){
			findQuery = queryCond;
			pageNumber = paging.getInteger("page_number") - 1;
			Integer skipLen = pageNumber * pageSize;
			findOptions.setSkip(skipLen);				
		}else{
		
			String matchSymbol = (sortDirection==1) ? "$gt" : "$lt";
			
			JsonObject latestValueCond = new JsonObject()
				.put(sortField, new JsonObject().put(matchSymbol, paging.getValue("latest_value")));				
			
			if(queryCond != null && queryCond.size() > 0){
				findQuery = new JsonObject();
				findQuery.put("$and", new JsonArray()
											.add(latestValueCond)
											.add(queryCond));
			}else{
				findQuery = latestValueCond;
			}
		}
		
		return new PagingOptions(findQuery, findOptions);

	}
	
	public static PagingOptions buildPagingOptions(JsonObject pagingWrapper){
		
		JsonObject fields = pagingWrapper.getJsonObject("fields");		
		JsonObject queryCond = pagingWrapper.getJsonObject("query");
		JsonObject pagingInfo = pagingWrapper.getJsonObject("paging");
		
		return buildPagingOptions(fields, pagingInfo, queryCond);
	}
}
