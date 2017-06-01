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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.i18n.CurrentTimeZone;

/**
 * You will only need to use it if displaying a localised date to the user, and
 * only when not using Freemarker, as FreemarkerSectionResult ensures the
 * localising transformation.
 * 
 * @author aholland
 */
@NonNullByDefault
public class LocalDate extends UtcDate
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param utcDate
	 * @param zone
	 */
	public LocalDate(UtcDate utcDate, TimeZone zone)
	{
		this(utcDate.toLong(), null, zone);
	}

	public LocalDate(TleDate date, TimeZone zone)
	{
		this(date.toLong(), date.getConceptualValue(), zone);
	}

	/**
	 * @param utc
	 * @param zone
	 */
	public LocalDate(long utc, TimeZone zone)
	{
		this(utc, null, zone);
	}

	/**
	 * @param date
	 * @param zone
	 */
	public LocalDate(Date date, TimeZone zone)
	{
		this(date.getTime(), null, zone);
	}

	/**
	 * @param calendar
	 * @param zone
	 */
	public LocalDate(Calendar calendar, TimeZone zone)
	{
		this(calendar.getTime().getTime(), null, zone);
	}

	/**
	 * @param localString
	 * @param dateFormat Note that if dateFormat contains a timezone, that will
	 *            be used to parse the date into utc, not the zone you supply.
	 * @param zone The zone to transform the localString with into utc, also
	 *            used when formatting this date
	 * @throws ParseException
	 */
	public LocalDate(String localString, Dates dateFormat, TimeZone zone) throws ParseException
	{
		this(new UtcDate(dateFormat.parse(localString, zone)), zone);
	}

	/**
	 * Uses Dates.ISO_WITH_TIMEZONE as the parsing format
	 * 
	 * @param isoUtcString
	 * @throws ParseException
	 */
	public LocalDate(String isoTimeZonedLocalString, TimeZone zone) throws ParseException
	{
		this(isoTimeZonedLocalString, Dates.ISO_WITH_TIMEZONE, zone);
	}

	protected LocalDate(long utc, @Nullable String conceptualDate, TimeZone zone)
	{
		super(utc, conceptualDate, zone);
	}

	/**
	 * @param zone
	 */
	public LocalDate(TimeZone zone)
	{
		this(new UtcDate(), zone);
	}

	public LocalDate()
	{
		this(new UtcDate(), CurrentTimeZone.get());
	}

	/**
	 * @return
	 */
	public UtcDate getUtc()
	{
		return new UtcDate(utc);
	}

	/**
	 * A ISO date (including timezone) representation of this date
	 */
	@Override
	public String toString()
	{
		return format(Dates.ISO_WITH_TIMEZONE);
	}

	@Override
	public String format(Dates dateFormat)
	{
		if( conceptualValue != null )
		{
			return super.format(dateFormat);
		}
		return dateFormat.format(toDate(), zone);
	}

	@Override
	public String formatOrNull(Dates dateFormat)
	{
		return dateFormat.formatOrNull(toDate(), zone);
	}

	@Override
	public LocalDate addDays(int days)
	{
		Calendar cal = Calendar.getInstance(zone);
		cal.setTime(new Date(toLong()));
		cal.add(Calendar.HOUR, 24 * days);
		return new LocalDate(cal.getTime(), zone);
	}

	public static UtcDate conceptualDate(Date date, TimeZone zone)
	{
		return new LocalDate(date, zone).conceptualDate();
	}
}
