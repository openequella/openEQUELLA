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

package com.tle.web.portal.standard.service;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.portal.entity.impl.PortletRecentContrib;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SearchResults;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;

/**
 * @author aholland
 */
@Bind(PortletStandardWebService.class)
@Singleton
public class PortletStandardWebServiceImpl implements PortletStandardWebService
{
	private static final int RECENT_MAX_RESULTS = 10;

	private final Cache<String, List<Item>> recentCache = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(30, TimeUnit.MINUTES).build();

	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private FreeTextService freetextService;

	@Override
	public List<Item> getRecentContributions(PortletRecentContrib portlet)
	{
		final String cacheKey = getRecentContribCacheKey(portlet);
		List<Item> results = recentCache.getIfPresent(cacheKey);
		if( results == null )
		{
			final DefaultSearch search = new DefaultSearch();
			final Collection<ItemDefinition> collections = portlet.getCollections();
			if( !Check.isEmpty(collections) )
			{
				search.setCollectionUuids(
					collectionService.convertToUuids(collectionService.filterSearchable(collections)));
			}
			search.setQuery(portlet.getQuery());
			search.setOwner(portlet.getUserId());
			search.setSortType(SortType.DATEMODIFIED);

			String status = portlet.getPortlet().getAttribute("status"); //$NON-NLS-1$
			if( !Check.isEmpty(status) )
			{
				search.setItemStatuses(ItemStatus.valueOf(status.toUpperCase()));
			}
			search.setNotItemStatuses(ItemStatus.PERSONAL);

			final int age = portlet.getAgeDays();
			if( age > 0 )
			{
				final Date now = new Date();
				final Date start = new Date(now.getTime() - TimeUnit.DAYS.toMillis(age));
				search.setDateRange(new Date[]{start, now});
			}

			final SearchResults<Item> searchResults = freetextService.search(search, 0, RECENT_MAX_RESULTS);
			results = searchResults.getResults();

			// TODO: use the cache? this will mean new items will be missed
			// until next login
			// perhaps have a refresh/update button on the portlet?
			// recentCache.put(cacheKey, results);
		}
		return results;
	}

	/**
	 * This needs to be a composite of user and portlet, i.e. different users
	 * may see different results
	 * 
	 * @return
	 */
	private String getRecentContribCacheKey(PortletRecentContrib portlet)
	{
		return CurrentUser.getUserID() + ':' + portlet.getPortlet().getUuid();
	}
}
