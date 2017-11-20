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

package com.tle.core.search.searchset.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.EntityScript;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.Check;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;

public class Gatherer
{
	private final Map<String, StringBuilder> map = new HashMap<String, StringBuilder>();

	public void addAll(Collection<? extends EntityScript<? extends BaseEntity>> queries)
	{
		if( !Check.isEmpty(queries) )
		{
			for( EntityScript<? extends BaseEntity> query : queries )
			{
				add(query);
			}
		}
	}

	public void add(EntityScript<? extends BaseEntity> query)
	{
		if( query != null )
		{
			add(query.getEntity().getUuid(), query.getScript());
		}
	}

	public void add(String entity, String script)
	{
		if( Check.isEmpty(script) )
		{
			if( !map.containsKey(entity) )
			{
				map.put(entity, null);
			}
		}
		else
		{
			StringBuilder existing = map.get(entity);
			if( existing == null )
			{
				existing = new StringBuilder();
				map.put(entity, existing);
			}
			else
			{
				existing.append(" and "); //$NON-NLS-1$
			}

			existing.append('(');
			existing.append(script);
			existing.append(')');
		}
	}

	public void addToQuery(FreeTextBooleanQuery orQuery, String field)
	{
		for( Map.Entry<String, StringBuilder> entry : map.entrySet() )
		{
			addToQuery(orQuery, field, entry.getKey(), entry.getValue());
		}
	}

	private static void addToQuery(FreeTextBooleanQuery orQuery, String field, String entityUuid, StringBuilder where)
	{
		FreeTextQuery peritemdef;
		FreeTextFieldQuery qitemdef = new FreeTextFieldQuery(field, entityUuid);
		if( where != null )
		{
			FreeTextBooleanQuery bquery = new FreeTextBooleanQuery(false, true);
			bquery.add(qitemdef);

			FreeTextBooleanQuery query = WhereParser.parse(where.toString());
			bquery.add(query);
			peritemdef = bquery;
		}
		else
		{
			peritemdef = qitemdef;
		}
		orQuery.add(peritemdef);
	}
}