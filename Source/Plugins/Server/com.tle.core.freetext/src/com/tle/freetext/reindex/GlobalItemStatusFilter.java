/*
 * Created on Sep 21, 2005
 */
package com.tle.freetext.reindex;

import com.tle.beans.item.ItemStatus;
import com.tle.freetext.reindexing.ReindexFilter;

/**
 * @author Nicholas Read
 */
public class GlobalItemStatusFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"status"};

	private Object[] values;

	public GlobalItemStatusFilter(ItemStatus itemStatus)
	{
		values = new Object[]{itemStatus.name()};
	}

	@Override
	protected String getWhereClause()
	{
		return "where status = :status";
	}

	@Override
	protected String[] getNames()
	{
		return NAMES;
	}

	@Override
	protected Object[] getValues()
	{
		return values;
	}
}
