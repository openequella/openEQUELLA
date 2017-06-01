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
