package com.tle.web.portal.section.admin;

import com.tle.common.portal.entity.Portlet;
import com.tle.common.searching.SearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class PortletSearchResultEvent extends AbstractSearchResultsEvent<PortletSearchResultEvent>
{
	private final SearchResults<Portlet> results;
	private final PortletSearchEvent searchEvent;
	private final int unfiltered;

	public PortletSearchResultEvent(SearchResults<Portlet> results, PortletSearchEvent searchEvent, int unfiltered)
	{
		this.results = results;
		this.searchEvent = searchEvent;
		this.unfiltered = unfiltered;
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

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchResultsListener<PortletSearchResultEvent> listener)
		throws Exception
	{
		listener.processResults(info, this);
	}

	public SearchResults<Portlet> getResults()
	{
		return results;
	}

	@Override
	public int getFilteredOut()
	{
		return unfiltered;
	}

	public PortletSearchEvent getSearchEvent()
	{
		return searchEvent;
	}
}
