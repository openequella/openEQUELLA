/*
 * Created on Jun 28, 2005
 */
package com.tle.core.freetext.queries;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.TleDate;

/**
 * @author jmaginnis
 */
@NonNullByDefault
public class DateRangeQuery extends BaseQuery
{
	private static final long serialVersionUID = 1L;

	private final String field;
	@Nullable
	private final TleDate start;
	@Nullable
	private final TleDate end;
	protected final String displayName;

	public DateRangeQuery(String field, TleDate start, TleDate end, String displayName)
	{
		this.field = field;
		this.start = start;
		this.end = end;
		this.displayName = displayName;
	}

	@SuppressWarnings("nls")
	@Nullable
	@Override
	public String getCriteriaText()
	{
		String fieldName = displayName;
		if( Check.isEmpty(fieldName) )
		{
			fieldName = field;
		}
		else
		{
			fieldName = "'" + fieldName + "'";
		}

		if( start == null && end == null )
		{
			return null;
		}
		else if( start == null )
		{
			return CurrentLocale.get("com.tle.core.entity.services.query.date.before", fieldName, format(end));
		}
		else if( end == null )
		{
			return CurrentLocale.get("com.tle.core.entity.services.query.date.after", fieldName, format(start));
		}
		return CurrentLocale.get("com.tle.core.entity.services.query.date.between", fieldName, format(start),
			format(end));
	}

	/**
	 * Only a display date
	 * 
	 * @param date
	 * @return
	 */
	private String format(TleDate date)
	{
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, CurrentLocale.getLocale());
		if( df instanceof SimpleDateFormat )
		{
			((SimpleDateFormat) df).setTimeZone(date.getTimeZone());
		}
		return df.format(date.toDate());
	}

	@Override
	public FreeTextQuery getFreeTextQuery()
	{
		return new FreeTextDateQuery(field, start, end, true, true);
	}
}
