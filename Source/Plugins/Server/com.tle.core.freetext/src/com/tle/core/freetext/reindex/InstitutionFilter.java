/*
 * Created on Sep 21, 2005
 */
package com.tle.core.freetext.reindex;

/**
 * @author Nicholas Read
 */
public class InstitutionFilter extends ReindexFilter
{
	private static final long serialVersionUID = 1L;

	private static final String[] NAMES = {};

	private Object[] values;

	public InstitutionFilter()
	{
		values = new Object[]{};
	}

	@Override
	protected String getWhereClause()
	{
		return "where 1 = 1"; //$NON-NLS-1$
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
