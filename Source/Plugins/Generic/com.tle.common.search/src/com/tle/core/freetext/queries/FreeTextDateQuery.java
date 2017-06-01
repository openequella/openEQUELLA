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
import java.util.TimeZone;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.i18n.CurrentTimeZone;
import com.tle.common.util.Dates;
import com.tle.common.util.TleDate;

/**
 * @author Nicholas Read
 */
public class FreeTextDateQuery extends FreeTextQuery
{
	private static final long serialVersionUID = 1L;

	private final String field;
	private final TleDate start;
	private final TleDate end;
	private final boolean includeEnd;
	private final boolean includeStart;

	public FreeTextDateQuery(String field, TleDate start, TleDate end, boolean includeStart, boolean includeEnd)
	{
		this.field = getRealField(field);
		this.start = start;
		this.end = end;
		this.includeStart = includeStart;
		this.includeEnd = includeEnd;
	}

	public String getField()
	{
		return field;
	}

	public TleDate getStart()
	{
		return start;
	}

	public TleDate getEnd()
	{
		return end;
	}

	public Dates getDateFormat()
	{
		return Dates.ISO_DATE_ONLY;
	}

	public TimeZone getTimeZone()
	{
		return CurrentTimeZone.get();
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	public boolean isIncludeEnd()
	{
		return includeEnd;
	}

	public boolean isIncludeStart()
	{
		return includeStart;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(field, start, end, includeStart, includeEnd);
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj == null || !(obj instanceof FreeTextDateQuery) )
		{
			return false;
		}
		else if( this == obj )
		{
			return true;
		}
		else
		{
			FreeTextDateQuery rhs = (FreeTextDateQuery) obj;
			return includeStart == rhs.includeStart && includeEnd == rhs.includeEnd && Objects.equals(field, rhs.field)
				&& Objects.equals(start, rhs.start) && Objects.equals(end, rhs.end);
		}
	}
}
