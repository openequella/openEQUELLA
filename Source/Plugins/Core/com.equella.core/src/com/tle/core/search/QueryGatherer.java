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
