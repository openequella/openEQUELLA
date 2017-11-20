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
public class FreeTextFieldQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private String field;
	private String value;
	private boolean tokenise;
	private boolean possibleWildcard;
	private boolean mustExist;

	public FreeTextFieldQuery(String field, String value)
	{
		this.field = getRealField(field);
		this.value = value;
	}

	public FreeTextFieldQuery(String field, String value, boolean mustExist)
	{
		this(field, value);
		this.mustExist = mustExist;
	}

	public String getField()
	{
		return field;
	}

	public String getValue()
	{
		return value;
	}

	public void setTokenise(boolean tokenise)
	{
		this.tokenise = tokenise;
	}

	public boolean isTokenise()
	{
		return tokenise;
	}

	public boolean isMustExist()
	{
		return mustExist;
	}

	public void setMustExist(boolean mustExist)
	{
		this.mustExist = mustExist;
	}

	public boolean isPossibleWildcard()
	{
		return possibleWildcard;
	}

	public void setPossibleWildcard(boolean possibleWildcard)
	{
		this.possibleWildcard = possibleWildcard;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(field, value, tokenise, possibleWildcard, mustExist);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextFieldQuery) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			FreeTextFieldQuery rhs = (FreeTextFieldQuery) obj;
			return tokenise == rhs.tokenise && possibleWildcard == rhs.possibleWildcard && mustExist == rhs.mustExist
				&& Objects.equals(field, rhs.field) && Objects.equals(value, rhs.value);
		}
	}
}
