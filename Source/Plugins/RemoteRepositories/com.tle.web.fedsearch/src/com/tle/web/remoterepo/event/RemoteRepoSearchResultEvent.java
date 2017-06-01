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

package com.tle.web.remoterepo.event;

import com.tle.common.Check;
import com.tle.common.searching.SearchResults;
import com.tle.core.fedsearch.RemoteRepoSearchResult;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class RemoteRepoSearchResultEvent<R extends RemoteRepoSearchResult>
	extends
		AbstractSearchResultsEvent<RemoteRepoSearchResultEvent<R>>
{
	private final SearchResults<R> results;

	public RemoteRepoSearchResultEvent(SearchResults<R> results)
	{
		this.results = results;
		String errorMessage = results.getErrorMessage();
		if( !Check.isEmpty(errorMessage) )
		{
			setErrored(true);
			setErrorMessage(errorMessage);
		}
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info,
		SearchResultsListener<RemoteRepoSearchResultEvent<R>> listener) throws Exception
	{
		listener.processResults(info, this);
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
	public int getOffset()
	{
		return results.getOffset();
	}

	public SearchResults<R> getResults()
	{
		return results;
	}

	@Override
	public int getFilteredOut()
	{
		return 0;
	}
}
