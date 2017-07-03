/*
 * Created on Sep 21, 2005
 */
package com.tle.core.freetext.reindex;

import com.tle.beans.entity.itemdef.ItemDefinition;

@SuppressWarnings("nls")
public class ItemdefFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"itemdef"};

	private Object[] values;

	public ItemdefFilter(ItemDefinition itemdef)
	{
		values = new Object[]{itemdef.getId()};
	}

	@Override
	protected String getWhereClause()
	{
		return "where itemDefinition.id = :itemdef";
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
