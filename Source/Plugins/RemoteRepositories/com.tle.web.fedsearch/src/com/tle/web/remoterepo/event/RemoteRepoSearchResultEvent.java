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
