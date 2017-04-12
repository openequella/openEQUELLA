/*
 * Created on Sep 21, 2005
 */
package com.tle.freetext.reindex;

import com.tle.freetext.reindexing.ReindexFilter;

/**
 * @author Nicholas Read
 */
public class ItemMetadataFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"targetId"};

	private Object[] values;

	public ItemMetadataFilter(String targetId)
	{
		values = new Object[]{targetId};
	}

	@Override
	protected String getWhereClause()
	{
		return "where :targetId in (metadataSecurityTargets)";
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
