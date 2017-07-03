/*
 * Created on Sep 21, 2005
 */
package com.tle.core.freetext.reindex;

import com.tle.beans.item.Item;

/**
 * @author Nicholas Read
 */
public class ItemFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"id"};

	private Object[] values;

	public ItemFilter(Item item)
	{
		values = new Object[]{item.getId()};
	}

	@Override
	protected String getWhereClause()
	{
		return "where id = :id";
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
