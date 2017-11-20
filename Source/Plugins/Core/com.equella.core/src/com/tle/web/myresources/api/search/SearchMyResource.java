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

package com.tle.web.myresources.api.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.interfaces.CsvList;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerItemBean;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.ItemResource;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.search.bean.SearchDefinitionBean;
import com.tle.web.myresources.MyResourcesSearch;
import com.tle.web.myresources.MyResourcesService;
import com.tle.web.myresources.MyResourcesSubSearch;
import com.tle.web.remoting.rest.service.UrlLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author Aaron & Dustin
 */
@Bind
@Path("search/myresources")
@Api(value = "/search/myresources", description = "search-myresources")
@Produces({"application/json"})
@SuppressWarnings("nls")
@Singleton
public class SearchMyResource
{
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemSerializerService itemSerializerService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private UrlLinkService urlLinkService;

	@Inject
	private MyResourcesService myResourcesService;

	@GET
	@Path("")
	public Response getMyResourcesSearchTypes()
	{
		List<MyResourcesSubSearch> listSearches = listAllowedMyResourcesSearches();
		return Response.ok(Lists.transform(listSearches, new Function<MyResourcesSubSearch, SearchDefinitionBean>()
		{

			@Override
			public SearchDefinitionBean apply(MyResourcesSubSearch input)
			{
				SearchDefinitionBean searchDefinitionBean = new SearchDefinitionBean();
				searchDefinitionBean.setName(CurrentLocale.get(input.getNameKey()));
				searchDefinitionBean.setId(input.getValue());
				searchDefinitionBean.set("links",
					urlLinkService.getMethodUriBuilder(SearchMyResource.class, "subSearch").build(input.getValue()));
				return searchDefinitionBean;
			}
		})).build();
	}

	@GET
	@Path("/{subsearch}")
	@ApiOperation(value = "Search within my resources")
	public Response subSearch(
		// @formatter:off
		@ApiParam(value="The id of the subsearch", allowableValues="published,draft,modqueue,archived,all,purchased", defaultValue="all")@PathParam("subsearch")
			String subsearch,
		@ApiParam(value="Query string", required = false) @QueryParam("q") 
			String q, 
		@ApiParam(value="The first record of the search results to return", required = false, defaultValue="0") @QueryParam("start") 
			int start,		
		@ApiParam(value="The number of results to return", required = false, defaultValue = "10", allowableValues = "range[1,50]") @QueryParam("length") 
			int length,		
		@ApiParam(value="List of collections", required = false) @QueryParam("collections") 
			CsvList collections,
		@ApiParam(value="The order of the search results", allowableValues="relevance,modified,name,rating", required = false) @QueryParam("order") 
			String order, 
		@ApiParam(value="Reverse the order of the search results", allowableValues = "true,false", defaultValue = "false", required = false) 
		@QueryParam("reverse") 
			String reverse,
		@ApiParam(value="The where-clause in the same format as the old SOAP one. See https://equella.github.io/",
					required = false) @QueryParam("where") 
			String where, 
		@ApiParam(value="How much information to return for the results", required = false, 
					allowableValues = ItemResource.ALL_ALLOWABLE_INFOS, 
					allowMultiple = true) @QueryParam("info") 
			CsvList info
		)
		// @formatter:on
	{
		Preconditions.checkArgument(length <= 50, "Length must be less than or equal to 50");
		final SearchBean<ItemBean> result = new SearchBean<ItemBean>();
		final List<ItemBean> resultItems = Lists.newArrayList();

		// sanitise parameters
		final String whereClause = where;
		final boolean onlyLive = false;
		final SortType orderType = DefaultSearch.getOrderType(order, q);
		final boolean reverseOrder = (reverse != null && Utils.parseLooseBool(reverse, false));
		final int offset = (start < 0 ? 0 : start);
		final int count = (length <= 0 ? 10 : length);
		final List<String> infos = CsvList.asList(info, ItemSerializerService.CATEGORY_BASIC);

		for( MyResourcesSubSearch myResourcesSubSearch : listAllowedMyResourcesSearches() )
		{
			if( myResourcesSubSearch.getValue().equals(subsearch) )
			{
				final MyResourcesSearch subSearch = myResourcesSubSearch.createDefaultSearch(null);
				final DefaultSearch search = createSearch(q, collections == null ? null : CsvList.asList(collections),
					whereClause, onlyLive, orderType, reverseOrder, subSearch);
				final SearchResults<ItemIdKey> searchResults = freetextService.searchIds(search, offset, count);

				final List<ItemIdKey> itemIds = searchResults.getResults();
				final List<Long> ids = Lists.transform(itemIds, new Function<ItemIdKey, Long>()
				{
					@Override
					public Long apply(ItemIdKey input)
					{
						return input.getKey();
					}
				});

				final ItemSerializerItemBean serializer = itemSerializerService.createItemBeanSerializer(ids, infos,
					false);
				for( ItemIdKey itemId : itemIds )
				{
					EquellaItemBean itemBean = new EquellaItemBean();
					itemBean.setUuid(itemId.getUuid());
					itemBean.setVersion(itemId.getVersion());
					serializer.writeItemBeanResult(itemBean, itemId.getKey());
					itemLinkService.addLinks(itemBean);
					resultItems.add(itemBean);
				}

				result.setStart(searchResults.getOffset());
				result.setLength(searchResults.getCount());
				result.setAvailable(searchResults.getAvailable());
				result.setResults(resultItems);
				return Response.ok(result).build();
			}
		}
		return Response.status(404).build();
	}

	private List<MyResourcesSubSearch> listAllowedMyResourcesSearches()
	{
		List<MyResourcesSubSearch> allSearches = myResourcesService.listSearches();
		List<MyResourcesSubSearch> allowedSearches = new ArrayList<MyResourcesSubSearch>();

		for( MyResourcesSubSearch myResourcesSubSearch : allSearches )
		{
			if( !isRestSearchable(myResourcesSubSearch) )
			{
				allowedSearches.add(myResourcesSubSearch);
			}
		}

		return allSearches;
	}

	private boolean isRestSearchable(MyResourcesSubSearch myResourcesSubSearch)
	// We might want to cut some out, new ones need to be explicitly added
	{
		String[] allowed = {"published", "draft", "modqueue", "archived", "all", "purchased"};
		for( String type : allowed )
		{
			if( myResourcesSubSearch.getValue().equals(type) )
			{
				return true;
			}
		}
		return false;
	}

	private DefaultSearch createSearch(String freetext, Collection<String> collectionUuids, String whereClause,
		boolean onlyLive, SortType sortType, boolean reverseOrder, DefaultSearch search)
	{
		search.setCollectionUuids(collectionUuids);
		if( !Check.isEmpty(whereClause) )
		{
			search.setFreeTextQuery(WhereParser.parse(whereClause));
		}
		if( onlyLive )
		{
			search.setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
		}
		search.setSortFields(sortType.getSortField(reverseOrder));
		search.setQuery(freetext);
		return search;
	}
}
