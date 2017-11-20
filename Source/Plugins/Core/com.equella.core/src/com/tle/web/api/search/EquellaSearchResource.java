/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.api.search;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.tle.common.interfaces.CsvList;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.search.interfaces.SearchResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * @author Aaron
 */
@Path("search")
@Api(value = "/search", description = "search")
@Produces(MediaType.APPLICATION_JSON)
public interface EquellaSearchResource extends SearchResource
{

	// TODO: JSON format javadoc
	// TODO: where format javadoc
	// TODO: all values of info param
	/**
	 * @param q The text query
	 * @param start The index of the first record to return. The first available
	 *            record is at 0
	 * @param length The number of results to return
	 * @param collections A comma separated list of collection UUIDs (optional).
	 *            If omitted, all collections will be searched.
	 * @param order One of: "relevance", "modified", "name", "rating"
	 * @param reverse Reverse the order of the results.
	 * @param where A complex where query.
	 * @param info A comma separated list of field groups to return.
	 * @param showall If true then includes draft items in the search results.
	 * @return JSON format
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Search for items", notes = "Search for items that the current user can discover")
	@ApiResponses({
			@ApiResponse(code = 200, message = "{\n  \"start\":0,\n  \"length\":0,\n  \"available\":0,\n  \"results\":[]\n}", response = SearchBean.class)})
	// @formatter:off
		SearchBean<ItemBean> searchItems(
			@Context UriInfo uriInfo,
			
			@ApiParam(value="Query string", required = false) 
			@QueryParam("q")
				String q,
			
			@ApiParam(value="The first record of the search results to return", required = false, defaultValue="0")
			@QueryParam("start")
				int start,
			
			@ApiParam(value="The number of results to return", required = false, defaultValue = "10", allowableValues = "range[0,50]")
			@QueryParam("length")
			@DefaultValue("10")
				int length,
			
			@ApiParam(value="List of collections", required = false)
			@QueryParam("collections")
				CsvList collections,
			
			@ApiParam(value="The order of the search results", allowableValues=",relevance,modified,name,rating", required = false)
			@QueryParam("order")
				String order,
			
			@ApiParam(value="Reverse the order of the search results", allowableValues = ",true,false", defaultValue = "false", required = false)
			@QueryParam("reverse")
				String reverse,
			
			@ApiParam(value="The where-clause in the same format as the old SOAP one. See https://equella.github.io/",
						required = false)
			@QueryParam("where")
				String where,
			
			@ApiParam(value="How much information to return for the results", required = false,
						allowableValues = ItemResource.ALL_ALLOWABLE_INFOS,
						allowMultiple = true)
			@QueryParam("info")
				CsvList info,
			
			@ApiParam(value="If true then includes items that are not live", allowableValues = ",true,false", defaultValue = "false", required = false)
			@QueryParam("showall")
				String showall,
			
			@ApiParam(value = "single dynamic collection uuid (:virtualized value)",
				  required = false)
			@QueryParam("dynacollection") String dynaCollectionCompound,
			
			@ApiParam(value = "Filter by item status", required = false, allowableValues="DRAFT,LIVE,REJECTED,MODERATING,ARCHIVED,SUSPENDED,DELETED,REVIEW,PERSONAL", allowMultiple = true)
			@QueryParam("status") 
				String status,
				
			@ApiParam(value = "An ISO date format (yyyy-MM-dd)", required = false)
			@QueryParam("modifiedAfter") 
				String modifiedAfter,
				
			@ApiParam(value = "An ISO date format (yyyy-MM-dd)", required = false)
			@QueryParam("modifiedBefore") 
				String modifiedBefore,
				
			@ApiParam(value = "An advanced search UUID. If a value is supplied, the collections in the advanced search will be used and the collections parameter will be ignored", required = false)
			@QueryParam("advancedsearch")
				String advancedSearch,
				
			@ApiParam(value = "An ID (not a username) of a user", required = false)
			@QueryParam("owner")
				String owner
			);
		// @formatter:on
}
