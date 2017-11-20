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

package com.tle.web.notification;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.interfaces.CsvList;
import com.tle.common.search.DefaultSearch;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.standard.indexer.NotificationResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.api.interfaces.beans.SearchBean;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.api.notification.interfaces.NotificationResource;
import com.tle.web.api.notification.interfaces.beans.NotificationApiBean;
import com.tle.web.workflow.portal.TaskListFilters;
import com.tle.web.workflow.portal.TaskListSubsearch;

/**
 * @author Aaron & Dustin
 */
@SuppressWarnings("nls")
@Bind(NotificationResource.class)
@Singleton
public class NotificationResourceImpl implements NotificationResource
{
	@Inject
	private FreeTextService freetextService;
	@Inject
	private ItemLinkService itemLinkService;
	@Inject
	private NotificationService notificationService;
	@Inject
	private TaskListFilters taskListFilters;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemOperationFactory workflowFactory;

	private final ImmutableMap<String, String> friendlyToUnfriendly = new ImmutableMap.Builder<String, String>()
		.put("all", "noteall").put("wentlive", "notewentlive").put("rejected", "noterejected")
		.put("badurl", "notebadurl").put("watchedwentlive", "notewentlive2").put("overdue", "noteoverdue")
		.put("itemsold", "notesold").put("error", "noteerror").put("executed", "noteexecuted").build();

	private final ImmutableMap<String, String> unfriendlyToFriendly = new ImmutableMap.Builder<String, String>()
		.put("wentlive", "wentlive").put("rejected", "rejected").put("badurl", "badurl")
		.put("wentliv2", "watchedwentlive").put("overdue", "overdue").put("itemsale", "itemsold")
		.put("piupdate", "piupdate").build();

	@Override
	public Response notificationsSearch(UriInfo uriInfo, String searchFilter, String q, int start, int length,
		CsvList collections)
	{
		final SearchBean<NotificationApiBean> result = new SearchBean<NotificationApiBean>();
		final List<NotificationApiBean> resultsToReturn = Lists.newArrayList();

		// sanitise parameters
		final int offset = (start < 0 ? 0 : start);
		final int count = (length <= 0 ? 10 : length);

		if( searchFilter == null )
		{
			searchFilter = "all";
		}

		searchFilter = friendlyToUnfriendly.get(searchFilter);

		final String filterId = (isRestSearchableNotification(searchFilter) ? searchFilter : "noteall");

		TaskListSubsearch notificationSearch = taskListFilters.getFilterForIdentifier(filterId);
		DefaultSearch search = notificationSearch.getSearch();
		search.setQuery(q);

		final FreetextSearchResults<NotificationResult> searchResults = freetextService.search(search, offset, count);

		final List<NotificationResult> actualSearchResults = Lists.newArrayList();
		for( int i = 0; i < searchResults.getCount(); i++ )
		{
			actualSearchResults.add(searchResults.getResultData(i));
		}

		for( NotificationResult notificationResult : actualSearchResults )
		{
			Notification notification = notificationService.getNotification(notificationResult.getNotificationId());
			NotificationApiBean notificationApiBean = new NotificationApiBean();

			//Ugh, make a migration and use UUIDs
			notificationApiBean.setId(notification.getId());
			notificationApiBean.setUuid(Long.toString(notification.getId()));

			String reason = unfriendlyToFriendly.get(notification.getReason());
			if( Check.isEmpty(reason) )
			{
				reason = notification.getReason();
			}

			notificationApiBean.setReason(reason);
			notificationApiBean.setDate(notification.getDate());
			notificationApiBean.setUserTo(notification.getUserTo());

			ItemBean itemBean = new ItemBean();
			itemBean.setUuid(notificationResult.getItemIdKey().getUuid());
			itemBean.setVersion(notificationResult.getItemIdKey().getVersion());
			itemLinkService.addLinks(itemBean);
			notificationApiBean.setItem(itemBean);

			resultsToReturn.add(notificationApiBean);
		}

		result.setStart(searchResults.getOffset());
		result.setLength(searchResults.getCount());
		result.setAvailable(searchResults.getAvailable());
		result.setResults(resultsToReturn);
		return Response.ok(result).build();
	}

	private boolean isRestSearchableNotification(String subsearch)
	{
		String[] allowed = {"noteall", "notewentlive", "noterejected", "notebadurl", "notewentlive2", "noteoverdue",
				"notesold", "noteerror", "noteexecuted"};
		for( String type : allowed )
		{
			if( type.equals(subsearch) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public Response delete(String notificationId, boolean waitForIndex)
	{
		try
		{
			//legacy (and unfortunately current) behaviour
			Long longId = Long.parseLong(notificationId);
			ItemId itemId = new ItemId(notificationService.getNotification(longId).getItemid());
			itemService.operation(itemId, workflowFactory.clearNotification(longId),
				workflowFactory.reindexOnly(waitForIndex));
			return Response.status(Status.NO_CONTENT).build();
		}
		catch( NumberFormatException nfe )
		{
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
}
