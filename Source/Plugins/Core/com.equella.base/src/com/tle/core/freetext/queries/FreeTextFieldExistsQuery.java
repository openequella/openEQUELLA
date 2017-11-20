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

import java.util.Objects;

import com.dytech.edge.queries.FreeTextQuery;

/**
 * @author jmaginnis
 */
public class FreeTextFieldExistsQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private String field;

	public FreeTextFieldExistsQuery(String field)
	{
		this.field = getRealField(field);
	}

	public String getField()
	{
		return field;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(field);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextFieldExistsQuery) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			FreeTextFieldExistsQuery rhs = (FreeTextFieldExistsQuery) obj;
			return Objects.equals(field, rhs.field);
		}
	}
}
