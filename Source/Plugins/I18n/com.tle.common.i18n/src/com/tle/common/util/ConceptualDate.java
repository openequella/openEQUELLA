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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.tle.annotation.Nullable;

/**
 * Work in progress.... time constrains me
 * 
 * @author Aaron
 */
public class ConceptualDate implements TleDate
{
	private final int day;
	private final int month;
	private final int year;

	/**
	 * @param day
	 * @param month NOT zero based like the Java one
	 * @param year
	 */
	public ConceptualDate(int day, int month, int year)
	{
		this.day = day;
		this.month = month;
		this.year = year;
	}

	@Override
	public int compareTo(TleDate o)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object obj)// NOSONAR see comment
	{
		// TODO Auto-generated method stub
		// should implement when/ if we implement compareTo ...
		return super.equals(obj);
	}

	@Override
	public int hashCode()// NOSONAR see comment
	{
		// TODO Auto-generated method stub
		// should implement when/ if we implement compareTo ...
		return super.hashCode();
	}

	@Override
	public Date toDate()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long toLong()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String format(Dates dateFormat)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Nullable
	public String formatOrNull(Dates dateFormat)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isConceptual()
	{
		return true;
	}

	// @Override
	// @Nullable
	// public String getConceptualValue()
	// {
	// // TODO Auto-generated method stub
	// return null;
	// }

	@Override
	public boolean before(TleDate otherDate)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean after(TleDate otherDate)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TleDate toMidnight()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TimeZone getTimeZone()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UtcDate conceptualDate()
	{
		// return new ConceptualDate(day, month, year);
		return null;
	}

	@Override
	public TleDate addDays(int days)
	{
		// TODO Auto-generated method stub
		Calendar c = Calendar.getInstance(DateHelper.UTC_TIMEZONE);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		c.add(Calendar.HOUR, 24);
		return null;
	}

	@Override
	@Nullable
	public String getConceptualValue()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
