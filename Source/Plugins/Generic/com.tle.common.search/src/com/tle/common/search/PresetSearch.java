package com.tle.common.search;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemStatus;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;

public class PresetSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	private final String queryText;
	private final FreeTextQuery freetext;
	private final boolean dynamicCollection;

	public PresetSearch(String query, FreeTextQuery freetext, boolean liveOnly)
	{
		this(query, freetext, liveOnly, false);
	}

	public PresetSearch(String query, FreeTextQuery freetext, boolean liveOnly, boolean dynamicCollection)
	{
		this.queryText = query;
		this.freetext = freetext;
		this.dynamicCollection = dynamicCollection;
		if( liveOnly )
		{
			setItemStatuses(ItemStatus.LIVE, ItemStatus.REVIEW);
		}
	}

	@Override
	public String getQuery()
	{
		return FreeTextQuery.combineQuery(query, queryText);
	}

	@Override
	public FreeTextQuery getFreeTextQuery()
	{
		return FreeTextBooleanQuery.get(false, true, freetext, freeTextQuery);
	}

	public String getQueryText()
	{
		return queryText;
	}

	public boolean isDynamicCollection()
	{
		return dynamicCollection;
	}
}
