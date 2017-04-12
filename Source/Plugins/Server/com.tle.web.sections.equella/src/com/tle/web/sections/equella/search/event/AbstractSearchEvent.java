package com.tle.web.sections.equella.search.event;

import com.tle.common.Check;
import com.tle.common.searching.SortField;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.events.AbstractTargettedEvent;

public abstract class AbstractSearchEvent<E extends AbstractSearchEvent<E>>
	extends
		AbstractTargettedEvent<E, SearchEventListener<E>>
{
	protected boolean loggable;
	protected String query;
	private int offset;
	private int count;
	protected boolean userFiltered;
	protected boolean keywordFiltered;
	protected boolean queryFiltered;
	protected boolean invalid;
	private boolean excludeKeywords;

	public void setSortFields(SortField... fields)
	{
		throw new UnsupportedOperationException();
	}

	public AbstractSearchEvent(SectionId sectionId)
	{
		super(sectionId);
	}

	public String getQuery()
	{
		return query;
	}

	public void filterByTextQuery(String query, boolean includeUnfiltered)
	{
		if( this.query != null )
		{
			throw new UnsupportedOperationException("Can't filter twice by keyword"); //$NON-NLS-1$
		}
		if( !Check.isEmpty(query) )
		{
			keywordFiltered = true;
		}
		this.query = query;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public final Class<SearchEventListener> getListenerClass()
	{
		return SearchEventListener.class;
	}

	public boolean isLoggable()
	{
		return loggable;
	}

	public void setLoggable(boolean loggable)
	{
		this.loggable = loggable;
	}

	public boolean isFiltered()
	{
		return userFiltered || keywordFiltered || queryFiltered;
	}

	public boolean isUserFiltered()
	{
		return userFiltered;
	}

	public boolean isKeywordFiltered()
	{
		return keywordFiltered;
	}

	public boolean isQueryFiltered()
	{
		return queryFiltered;
	}

	public void setQueryFiltered(boolean queryFiltered)
	{
		this.queryFiltered = queryFiltered;
	}

	public String getSearchedText()
	{
		return query == null ? "" : query;
	}

	public void setUserFiltered(boolean userFiltered)
	{
		this.userFiltered = userFiltered;
	}

	public boolean isInvalid()
	{
		return invalid;
	}

	public void setInvalid(boolean invalid)
	{
		this.invalid = invalid;
	}

	public boolean isExcludeKeywords()
	{
		return excludeKeywords;
	}

	public void setExcludeKeywords(boolean excludeKeywords)
	{
		this.excludeKeywords = excludeKeywords;
	}
}
