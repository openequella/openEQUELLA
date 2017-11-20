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

package com.tle.web.connectors.manage;

import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class ConnectorManagmentSearchResultEvent
	extends
		AbstractSearchResultsEvent<ConnectorManagmentSearchResultEvent>
{

	private final SearchResults<ConnectorContent> results;
	private final FreetextSearchResults<FreetextResult> search;
	private final ConnectorManagementSearchEvent event;
	private final Connector connector;
	private int unfiltered;

	public ConnectorManagmentSearchResultEvent(ConnectorManagementSearchEvent event,
		SearchResults<ConnectorContent> results, FreetextSearchResults<FreetextResult> search, int unfiltered,
		Connector connector)
	{
		super();
		this.results = results;
		this.search = search;
		this.unfiltered = unfiltered;
		this.connector = connector;
		this.event = event;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info,
		SearchResultsListener<ConnectorManagmentSearchResultEvent> listener) throws Exception
	{
		listener.processResults(info, this);
	}

	@Override
	public int getOffset()
	{
		return results.getOffset();
	}

	@Override
	public int getCount()
	{
		return results.getCount();
	}

	@Override
	public int getMaximumResults()
	{
		return results.getAvailable();
	}

	@Override
	public int getFilteredOut()
	{
		return unfiltered;
	}

	public SearchResults<ConnectorContent> getResults()
	{
		return results;
	}

	public FreetextSearchResults<FreetextResult> getSearch()
	{
		return search;
	}

	public Connector getConnector()
	{
		return connector;
	}

	public ConnectorManagementSearchEvent getEvent()
	{
		return event;
	}

}
