package com.tle.core.search;

import com.tle.common.Check;

@SuppressWarnings("nls")
public class QueryGatherer
{
	private final StringBuilder acc = new StringBuilder();
	private final String operator;

	public QueryGatherer(boolean and)
	{
		this.operator = and ? " AND " : " OR ";
	}

	public void add(String query)
	{
		if( !Check.isEmpty(query) )
		{
			if( acc.length() > 0 )
			{
				acc.append(operator);
			}

			acc.append('(');
			acc.append(query);
			acc.append(')');
		}
	}

	@Override
	public String toString()
	{
		return acc.toString();
	}
}
