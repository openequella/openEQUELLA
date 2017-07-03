/*
 * Created on Jul 14, 2004 For "The Learning Edge"
 */
package com.tle.core.freetext.reindex;

import java.io.Serializable;

import com.tle.core.item.service.ItemService;

/**
 * @author jmaginnis
 */
public abstract class ReindexFilter implements Serializable
{
	public void updateIndexTimes(ItemService itemService)
	{
		itemService.updateIndexTimes(getWhereClause(), getNames(), getValues());
	}

	protected abstract String getWhereClause();

	protected abstract String[] getNames();

	protected abstract Object[] getValues();

}
