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

package com.tle.web.search.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.DateFilter;
import com.tle.common.searching.SortField;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;

public class FreetextSearchEvent extends AbstractSearchEvent<FreetextSearchEvent>
{
	private final DefaultSearch search;
	private final DefaultSearch unfiltered;
	private String unfilteredQuery;
	private Exception exception;

	public FreetextSearchEvent(DefaultSearch search, DefaultSearch unfiltered)
	{
		super(null); // broadcast
		this.search = search;
		this.unfiltered = unfiltered;
	}

	@SuppressWarnings("unchecked")
	public <T extends DefaultSearch> T getDefaultSeach()
	{
		return (T) search;
	}

	public DefaultSearch getUnfilteredSearch()
	{
		unfiltered.setQuery(unfilteredQuery);
		return unfiltered;
	}

	public DefaultSearch getFinalSearch()
	{
		search.setQuery(query);
		return search;
	}

	public DefaultSearch getRawSearch()
	{
		return search;
	}

	public void filterByCollection(Collection<String> collectionUuids)
	{
		if( !Check.isEmpty(collectionUuids) )
		{
			userFiltered = true;
		}
		search.setCollectionUuids(collectionUuids);
	}

	public void filterByCollection(String... collections)
	{
		filterByCollection(Arrays.asList(collections));
	}

	@Override
	public void filterByTextQuery(String subQuery, boolean includeUnfiltered)
	{
		if( Check.isEmpty(subQuery) )
		{
			return;
		}
		keywordFiltered = true;
		query = FreeTextQuery.combineQuery(query, subQuery);
		if( includeUnfiltered )
		{
			unfilteredQuery = FreeTextQuery.combineQuery(unfilteredQuery, subQuery);
		}
	}

	public void filterByTerm(boolean not, String field, String value)
	{
		if( not )
		{
			search.addMustNot(field, value);
		}
		else
		{
			search.addMust(field, value);
		}
		userFiltered = true;
	}

	public void filterByTerms(boolean not, String field, Collection<String> values)
	{
		if( not )
		{
			search.addMustNot(field, values);
		}
		else
		{
			search.addMust(field, values);
		}
		userFiltered = true;
	}

	public void filterByOwner(String owner)
	{
		if( !Check.isEmpty(owner) )
		{
			userFiltered = true;
		}
		search.setOwner(owner);
	}

	public void filterByDateRange(Date[] dateRange)
	{
		userFiltered = true;
		search.setDateRange(dateRange);
	}

	public void filterByStatus(ItemStatus... statuses)
	{
		if( !Check.isEmpty(statuses) )
		{
			userFiltered = true;
		}
		search.setItemStatuses(Arrays.asList(statuses));
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchEventListener<FreetextSearchEvent> listener)
		throws Exception
	{
		listener.prepareSearch(info, this);
	}

	public Exception getException()
	{
		return exception;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
	}

	@Override
	public void setSortFields(boolean reversed, SortField... sort)
	{
		search.setSortFields(sort);
		search.setSortReversed(reversed);
	}

	public void addDateFilter(DateFilter filter)
	{
		search.addDateFilter(filter);
		userFiltered = true;
	}
}
