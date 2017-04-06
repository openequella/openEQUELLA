package com.tle.web.sections.equella.search.event;

import java.util.EventListener;

import com.tle.web.sections.events.AbstractSectionEvent;

public abstract class AbstractSearchResultsEvent<E extends AbstractSearchResultsEvent<E>>
	extends
		AbstractSectionEvent<SearchResultsListener<E>>
{
	protected boolean errored;
	protected String errorMessage;

	public boolean isErrored()
	{
		return errored;
	}

	public void setErrored(boolean errored)
	{
		this.errored = errored;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public abstract int getOffset();

	public abstract int getCount();

	public abstract int getMaximumResults();

	public abstract int getFilteredOut();

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return SearchResultsListener.class;
	}
}
