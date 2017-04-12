package com.tle.core.freetext.queries;

import java.util.Objects;

import com.dytech.edge.queries.FreeTextQuery;

public class FreeTextAutocompleteQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private String query;

	public FreeTextAutocompleteQuery(String queryText)
	{
		this.query = queryText;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(query);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextAutocompleteQuery) )
		{
			return false;
		}
		return this == obj || Objects.equals(query, ((FreeTextAutocompleteQuery) obj).query);
	}
}
