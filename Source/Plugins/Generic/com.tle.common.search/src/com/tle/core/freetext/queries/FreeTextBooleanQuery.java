/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.freetext.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.dytech.edge.queries.FreeTextQuery;

/**
 * @author jmaginnis
 */
public class FreeTextBooleanQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private final List<FreeTextQuery> clauses = new ArrayList<FreeTextQuery>();

	private boolean not;
	private boolean and;

	public FreeTextBooleanQuery(boolean not, boolean and)
	{
		this.not = not;
		this.and = and;
	}

	public FreeTextBooleanQuery(boolean not, boolean and, FreeTextQuery... clauses)
	{
		this(not, and);

		for( FreeTextQuery clause : clauses )
		{
			add(clause);
		}
	}

	public FreeTextBooleanQuery add(FreeTextQuery query)
	{
		if( query != null )
		{
			clauses.add(query);
		}
		return this;
	}

	public List<FreeTextQuery> getClauses()
	{
		return clauses;
	}

	public boolean isAnd()
	{
		return and;
	}

	public void setAnd(boolean and)
	{
		this.and = and;
	}

	public boolean isNot()
	{
		return not;
	}

	public void setNot(boolean not)
	{
		this.not = not;
	}

	public static FreeTextQuery get(boolean not, boolean and, FreeTextQuery q1, FreeTextQuery q2)
	{
		if( q2 == null )
		{
			return q1;
		}
		if( q1 != null )
		{
			return new FreeTextBooleanQuery(not, and, q1, q2);
		}
		else
		{
			return q2;
		}
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(clauses, not, and);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextBooleanQuery) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			FreeTextBooleanQuery rhs = (FreeTextBooleanQuery) obj;
			return not == rhs.not && and == rhs.and && Objects.equals(clauses, rhs.clauses);
		}
	}
}
