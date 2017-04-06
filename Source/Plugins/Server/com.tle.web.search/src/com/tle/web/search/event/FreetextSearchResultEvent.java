package com.tle.web.search.event;

import com.tle.core.services.item.FreetextResult;
import com.tle.core.services.item.FreetextSearchResults;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchResultsListener;

public class FreetextSearchResultEvent extends AbstractSearchResultsEvent<FreetextSearchResultEvent>
{
	private FreetextSearchResults<? extends FreetextResult> results;
	private final FreetextSearchEvent searchEvent;
	private Throwable exception;
	private int unfiltered;

	public FreetextSearchResultEvent(FreetextSearchResults<? extends FreetextResult> results,
		FreetextSearchEvent searchEvent, int unfiltered)
	{
		this.results = results;
		this.searchEvent = searchEvent;
		this.unfiltered = unfiltered;
	}

	public FreetextSearchResultEvent(Throwable e, FreetextSearchEvent searchEvent)
	{
		this.searchEvent = searchEvent;
		this.exception = e;
		errored = true;
	}

	@Override
	public boolean isErrored()
	{
		return errored;
	}

	public FreetextSearchResults<? extends FreetextResult> getResults()
	{
		return results;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SearchResultsListener<FreetextSearchResultEvent> listener)
		throws Exception
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

	public FreetextSearchEvent getSearchEvent()
	{
		return searchEvent;
	}

	public Throwable getException()
	{
		return exception;
	}

	@Override
	public int getFilteredOut()
	{
		return unfiltered;
	}
}
