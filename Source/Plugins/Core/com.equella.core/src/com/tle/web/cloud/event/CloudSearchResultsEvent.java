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

package com.tle.web.cloud.event;

import com.tle.core.cloud.service.CloudSearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class CloudSearchResultsEvent extends AbstractSearchResultsEvent<CloudSearchResultsEvent>
{
	private final CloudSearchResults results;
	private final int filteredOut;
	private final CloudSearchEvent event;

	public CloudSearchResultsEvent(CloudSearchEvent searchEvent, CloudSearchResults results, int filteredOut)
	{
		this.results = results;
		this.filteredOut = filteredOut;
		this.event = searchEvent;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchResultsListener<CloudSearchResultsEvent> listener)
		throws Exception
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
		return filteredOut;
	}

	public CloudSearchResults getResults()
	{
		return results;
	}

	public CloudSearchEvent getEvent()
	{
		return event;
	}
}