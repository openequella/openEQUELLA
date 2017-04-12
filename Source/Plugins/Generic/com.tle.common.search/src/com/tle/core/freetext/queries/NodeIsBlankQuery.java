/*
 * Created on Jun 30, 2005
 */
package com.tle.core.freetext.queries;

import java.util.Collection;

import com.dytech.edge.queries.FreeTextQuery;

/**
 * @author jmaginnis
 */
public class NodeIsBlankQuery extends BaseQuery
{
	private static final long serialVersionUID = 1L;
	protected boolean not;
	protected Collection<String> fields;
	protected boolean tokenise;

	public NodeIsBlankQuery(boolean not, Collection<String> fields)
	{
		if( fields == null || fields.size() == 0 )
		{
			throw new IllegalArgumentException("fields parameter must not be empty"); //$NON-NLS-1$
		}

		this.not = not;
		this.fields = fields;
	}

	@Override
	public FreeTextQuery getFreeTextQuery()
	{
		// FIXME: multiple fields
		FreeTextFieldQuery query = new FreeTextFieldQuery(fields.iterator().next(), ""); //$NON-NLS-1$
		query.setTokenise(tokenise);
		if( !not )
		{
			return query;
		}
		else
		{
			FreeTextBooleanQuery boolquery = new FreeTextBooleanQuery(not, false);
			boolquery.add(query);
			return boolquery;
		}
	}

	public void setTokenise(boolean tokenise)
	{
		this.tokenise = tokenise;
	}
}
