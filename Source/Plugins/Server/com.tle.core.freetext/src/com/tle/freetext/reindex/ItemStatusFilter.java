/*
 * Created on Sep 21, 2005
 */
package com.tle.freetext.reindex;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.freetext.reindexing.ReindexFilter;

/**
 * @author Nicholas Read
 */
public class ItemStatusFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"status", "itemDefinition"};

	private Object[] values;

	public ItemStatusFilter(ItemStatus itemStatus, ItemDefinition itemDefinition)
	{
		values = new Object[]{itemStatus.name(), itemDefinition.getId()};
	}

	@Override
	protected String getWhereClause()
	{
		return "where status = :status and itemDefinition.id = :itemDefinition";
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
