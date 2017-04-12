package com.tle.web.mimetypes.search.event;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.searching.SearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class MimeSearchResultEvent extends AbstractSearchResultsEvent<MimeSearchResultEvent>
{
	private final SearchResults<MimeEntry> results;

	public MimeSearchResultEvent(SearchResults<MimeEntry> results)
	{
		this.results = results;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchResultsListener<MimeSearchResultEvent> listener)
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

	public SearchResults<MimeEntry> getResults()
	{
		return results;
	}

	@Override
	public int getFilteredOut()
	{
		return 0;
	}
}
