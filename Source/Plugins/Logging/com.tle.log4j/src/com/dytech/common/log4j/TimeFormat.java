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

package com.dytech.common.log4j;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

class TimeFormat extends DateFormat
{
	private static long previousTime;
	private static char previousTimeWithoutMillis[] = new char[9];

	public TimeFormat()
	{
		setCalendar(Calendar.getInstance());
	}

	public TimeFormat(TimeZone timeZone)
	{
		setCalendar(Calendar.getInstance(timeZone));
	}

	@Override
	public StringBuffer format(Date date, StringBuffer sbuf, FieldPosition fieldPosition)
	{
		long now = date.getTime();
		long millis = now % 1000L;
		if( now - millis != previousTime )
		{
			calendar.setTime(date);
			int start = sbuf.length();
			int hour = calendar.get(11);
			if( hour < 10 )
			{
				sbuf.append('0');
			}
			sbuf.append(hour);
			sbuf.append(':');
			int mins = calendar.get(12);
			if( mins < 10 )
			{
				sbuf.append('0');
			}
			sbuf.append(mins);
			sbuf.append(':');
			int secs = calendar.get(13);
			if( secs < 10 )
			{
				sbuf.append('0');
			}
			sbuf.append(secs);
			sbuf.append('.');
			sbuf.getChars(start, sbuf.length(), previousTimeWithoutMillis, 0);
			previousTime = now - millis;
		}
		else
		{
			sbuf.append(previousTimeWithoutMillis);
		}

		if( millis < 100 )
		{
			sbuf.append('0');
		}

		if( millis < 10 )
		{
			sbuf.append('0');
		}

		sbuf.append(millis);
		return sbuf;
	}

	@Override
	public Date parse(String s, ParsePosition pos)
	{
		return null;
	}
}