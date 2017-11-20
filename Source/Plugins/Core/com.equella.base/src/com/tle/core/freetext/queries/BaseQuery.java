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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;

/**
 * @author jmaginnis
 */
public abstract class BaseQuery implements Serializable
{
	private List<BaseQuery> andFilters;
	private boolean not = false;
	private boolean and = true;

	public BaseQuery()
	{
		super();
	}

	public BaseQuery(BaseQuery query)
	{
		addQuery(query);
	}

	public void setIsNot(boolean not)
	{
		this.not = not;
	}

	public void setBooleanType(boolean and)
	{
		this.and = and;
	}

	public void addQuery(BaseQuery query)
	{
		if( query != null )
		{
			if( andFilters == null )
			{
				andFilters = new ArrayList<BaseQuery>(3);
			}
			andFilters.add(query);
		}
	}

	public String getCriteriaText()
	{
		return null;
	}

	public void addCriteria(List<String> criteriaList)
	{
		String criteriaText = getCriteriaText();
		if( criteriaText != null )
		{
			criteriaList.add(criteriaText);
		}
		if( andFilters != null )
		{
			for( BaseQuery query : andFilters )
			{
				query.addCriteria(criteriaList);
			}
		}
	}

	public FreeTextQuery getFullFreeTextQuery()
	{
		Collection<BaseQuery> filters = andFilters;
		if( filters == null )
		{
			filters = Collections.emptyList();
		}

		List<FreeTextQuery> queries = new ArrayList<FreeTextQuery>(filters.size() + 1);
		FreeTextQuery q = getFreeTextQuery();
		if( q != null )
		{
			queries.add(q);
		}

		for( BaseQuery query : filters )
		{
			q = query.getFullFreeTextQuery();
			if( q != null )
			{
				queries.add(q);
			}
		}

		int queryCount = queries.size();
		if( queryCount == 0 )
		{
			return null;
		}

		FreeTextQuery first = queries.get(0);
		if( queryCount == 1 && first instanceof FreeTextBooleanQuery )
		{
			FreeTextBooleanQuery fbool = (FreeTextBooleanQuery) first;
			fbool.setNot(fbool.isNot() ^ not);
			return fbool;
		}

		FreeTextBooleanQuery overall = new FreeTextBooleanQuery(not, and);
		for( FreeTextQuery ftq : queries )
		{
			overall.add(ftq);
		}

		return overall;
	}

	public FreeTextQuery getFreeTextQuery()
	{
		return null;
	}
}
