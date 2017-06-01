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
import java.util.Collections;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.Check;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author jmaginnis
 */
public class NodeInQuery extends NodeIsBlankQuery
{
	private static final long serialVersionUID = 1L;
	protected final Collection<String> values;
	protected final String displayName;

	public NodeInQuery(String value, boolean not, Collection<String> fields, String displayName)
	{
		this(Collections.singletonList(value), not, fields, displayName);
	}

	public NodeInQuery(Collection<String> values, boolean not, Collection<String> fields, String displayName)
	{
		super(not, fields);
		this.values = values;
		this.displayName = displayName;
	}

	@SuppressWarnings("nls")
	@Override
	public String getCriteriaText()
	{
		final String orWithSpace = " " + CurrentLocale.get("com.tle.core.entity.services.query.or") + " ";

		String fieldName = displayName;
		if( Check.isEmpty(fieldName) )
		{
			fieldName = Utils.join(fields.toArray(), orWithSpace);
		}

		boolean allEmpty = true;
		for( String value : values )
		{
			if( !Check.isEmpty(value) )
			{
				allEmpty = false;
			}
		}

		if( allEmpty )
		{
			return null;
		}

		return CurrentLocale.get("com.tle.core.entity.services.query.contains", fieldName,
			Utils.join(values.toArray(), orWithSpace));
	}

	@Override
	public FreeTextQuery getFreeTextQuery()
	{
		if( values.isEmpty() )
		{
			return null;
		}

		if( values.size() == 1 && !not )
		{
			final String value = values.iterator().next();
			if( Check.isEmpty(value) )
			{
				return null;
			}

			// 1 field, 1 value
			if( fields.size() == 1 )
			{
				final String field = fields.iterator().next();
				final FreeTextFieldQuery query = new FreeTextFieldQuery(field, value);
				query.setTokenise(tokenise);
				return query;
			}

			// multi fields, 1 value
			final FreeTextBooleanQuery or = new FreeTextBooleanQuery(not, false);
			for( String field : fields )
			{
				final FreeTextFieldQuery query = new FreeTextFieldQuery(field, values.iterator().next());
				query.setTokenise(tokenise);
				or.add(query);
			}
			return or;
		}

		// 1 field, multi values OR 1 field, 1 value with 'not'
		if( fields.size() == 1 )
		{
			final String field = fields.iterator().next();

			final FreeTextBooleanQuery or = new FreeTextBooleanQuery(not, false);
			boolean allEmpty = true;
			for( String value : values )
			{
				if( !Check.isEmpty(value) )
				{
					allEmpty = false;
					final FreeTextFieldQuery query = new FreeTextFieldQuery(field, value);
					query.setTokenise(tokenise);
					or.add(query);
				}
			}
			if( allEmpty )
			{
				return null;
			}
			return or;

		}

		// multi fields, multi values
		final FreeTextBooleanQuery or = new FreeTextBooleanQuery(not, false);
		boolean allEmpty = true;
		for( String field : fields )
		{
			final FreeTextBooleanQuery valueMixer = new FreeTextBooleanQuery(false, false);
			boolean fieldAllEmpty = true;
			for( String value : values )
			{
				if( !Check.isEmpty(value) )
				{
					fieldAllEmpty = false;
					allEmpty = false;
					FreeTextFieldQuery query = new FreeTextFieldQuery(field, value);
					query.setTokenise(tokenise);
					valueMixer.add(query);
				}
			}
			if( !fieldAllEmpty )
			{
				or.add(valueMixer);
			}
		}
		if( allEmpty )
		{
			return null;
		}
		return or;
	}
}
