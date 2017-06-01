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

package com.tle.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.dytech.common.GeneralConstants;
import com.dytech.common.threading.ThreadSafeSimpleDateFormat;
import com.dytech.common.threading.ThreadSafeSimpleDateFormat.BasicDateFormatFactory;
import com.dytech.common.threading.ThreadSafeSimpleDateFormat.DateFormatFactory;
import com.dytech.common.threading.ThreadSafeSimpleDateFormat.LocaleProvider;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
public enum Dates
{
	ISO(GeneralConstants.ISO_DATE_FORMAT_STR), ISO_NO_TIMEZONE(GeneralConstants.ISO_DATE_FORMAT_STR + "'Z'"),
	ISO_WITH_MILLIS_NO_TIMEZONE(GeneralConstants.ISO_DATE_FORMAT_STR + ".sss'Z'"), ISO_DATE_ONLY("yyyy-MM-dd"),
	ISO_WITH_TIMEZONE(GeneralConstants.ISO_DATE_FORMAT_STR + "Z"), ISO_WITH_GENERAL_TIMEZONE("yyyy-MM-dd'T'HH:mm:ssz"),
	ISO_MIDNIGHT("yyyy-MM-dd'T00:00:00'"), DATE_ONLY(DateFormat.LONG, -1), DATE_ONLY_FULL(DateFormat.FULL, -1),
	DATE_AND_TIME(DateFormat.LONG, DateFormat.SHORT), ACTIVE_CACHE_TIMESLOT("H:mm"), MERLOT("MMM dd, yyyy"),
	@Deprecated
	CALENDAR_CONTROL_FORM("dd/MM/yyyy");

	// // IMPLEMENTATION ///////////////////////////////////////////////////////

	private final ThreadSafeSimpleDateFormat instance;
	@Nullable
	private String formatStr;

	private Dates(String format)
	{
		this(new BasicDateFormatFactory(format));
		formatStr = format;
	}

	private Dates(final int dateStyle, final int timeStyle)
	{
		this(new DateFormatFactory()
		{
			@Override
			public DateFormat createDateFormat(Locale locale)
			{
				if( timeStyle < 0 )
				{
					return DateFormat.getDateInstance(dateStyle, locale);
				}
				return DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
			}
		});
	}

	private Dates(DateFormatFactory dateFormatFactory)
	{
		instance = new ThreadSafeSimpleDateFormat(dateFormatFactory, new LocaleProvider()
		{
			@Override
			public Locale getCurrentLocale()
			{
				return CurrentLocale.getLocale();
			}
		});
	}

	/**
	 * Package protected. Use UtcDate and LocalDate instead
	 * 
	 * @param date
	 * @param tz
	 * @return
	 */
	String format(Date date, TimeZone tz)
	{
		return instance.format(date, tz);
	}

	/**
	 * Package protected. Use UtcDate and LocalDate instead
	 * 
	 * @param date
	 * @param tz
	 * @return
	 */
	@Nullable
	String formatOrNull(@Nullable Date date, TimeZone tz)
	{
		return date == null ? null : format(date, tz);
	}

	/**
	 * Package protected. Use UtcDate and LocalDate instead
	 * 
	 * @param date
	 * @param tz
	 * @return
	 * @throws ParseException
	 */
	Date parse(String date, TimeZone tz) throws ParseException
	{
		return instance.parse(date, tz);
	}

	/**
	 * Package protected. Use UtcDate and LocalDate instead
	 * 
	 * @param date
	 * @param tz
	 * @return
	 */
	@Nullable
	Date parseOrNull(String date, TimeZone tz)
	{
		try
		{
			return parse(date, tz);
		}
		catch( ParseException ex )
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		return formatStr != null ? formatStr : super.toString();
	}
}