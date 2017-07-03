/*
 * Created on Sep 21, 2005
 */
package com.tle.core.freetext.reindex;

import com.tle.beans.entity.Schema;

public class SchemaFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {"schema"};

	private Object[] values;

	public SchemaFilter(Schema schema)
	{
		values = new Object[]{schema.getId()};
	}

	@Override
	protected String getWhereClause()
	{
		return "where itemDefinition in (from ItemDefinition where schema.id = :schema) ";
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
