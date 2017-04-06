/*
 * Created on Jun 29, 2005
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
