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

package com.tle.common.settings.standard;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;

/**
 * @author larry
 */
public class CourseDefaultsSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = -883052925969453965L;

	/**
	 * It would appear this is the format pre-existing course default dates have
	 * been stored in EQUELLA's history. It's the deprecated format from the
	 * com.tle.common.util.Dates.CALENDAR_CONTROL_FORM.
	 */
	public static final String COURSE_DEFAULT_DATE_FMT = "dd/MM/yyyy";
	/**
	 * Persisted data need be primitive ...?
	 */
	@Property(key = "course.start")
	private String startDate;

	@Property(key = "course.end")
	private String endDate;

	@Property(key = "portion.restrictions.enabled")
	private boolean portionRestrictionsEnabled;

	public boolean isPortionRestrictionsEnabled()
	{
		return portionRestrictionsEnabled;
	}

	public void setPortionRestrictionsEnabled(boolean portionRestrictionsEnabled)
	{
		this.portionRestrictionsEnabled = portionRestrictionsEnabled;
	}

	/**
	 * On a parse error, rethrow
	 * 
	 * @param dateAsString
	 * @return A UTC Date
	 */
	@Nullable
	public static Date parseDate(@Nullable String dateAsString) throws ParseException
	{
		if( Check.isEmpty(dateAsString) )
		{
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(COURSE_DEFAULT_DATE_FMT);
		dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		return dateFormat.parse(dateAsString);
	}

	/**
	 * On a parse error, rethrow
	 * 
	 * @param java.util.Date
	 * @return dateAsString
	 */
	public static String formatDateToPlainString(@Nullable Date date)
	{
		if( date == null )
		{
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(COURSE_DEFAULT_DATE_FMT);
		dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		return dateFormat.format(date);
	}

	/**
	 * @return the startDate
	 */
	@Nullable
	public String getStartDate()
	{
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(@Nullable String startDate)
	{
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	@Nullable
	public String getEndDate()
	{
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(@Nullable String endDate)
	{
		this.endDate = endDate;
	}
}
