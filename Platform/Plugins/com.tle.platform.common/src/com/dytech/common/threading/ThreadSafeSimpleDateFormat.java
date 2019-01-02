/*
 * Copyright 2019 Apereo
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

package com.dytech.common.threading;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.tle.common.Check;

/*
 * Keeps a pool of SimpleDateFormat objects. If more date formatters are
 * requested then the pool holds, then more will be created rather than
 * blocking. If more date formatters are released than the pool will hold, then
 * they are simply thrown away.
 * @author nick
 */
public class ThreadSafeSimpleDateFormat
{
	private final DateFormatFactory dateFormatFactory;
	private final LocaleProvider localeProvider;
	private final int poolSizePerLocale;

	private final Object mutex = new Object();

	/**
	 * LinkedList implementing at least the Deque interface
	 */
	private transient Map<Locale, Deque<DateFormat>> pool;

	public ThreadSafeSimpleDateFormat(final String format)
	{
		this(new BasicDateFormatFactory(format));
	}

	public ThreadSafeSimpleDateFormat(DateFormatFactory dateFormatFactory)
	{
		this(dateFormatFactory, new LocaleProvider()
		{
			@Override
			public Locale getCurrentLocale()
			{
				return Locale.getDefault();
			}
		});
	}

	public ThreadSafeSimpleDateFormat(String format, LocaleProvider localeProvider)
	{
		this(new BasicDateFormatFactory(format), localeProvider);
	}

	public ThreadSafeSimpleDateFormat(DateFormatFactory dateFormatFactory, LocaleProvider localeProvider)
	{
		this(dateFormatFactory, localeProvider, 5);
	}

	public ThreadSafeSimpleDateFormat(DateFormatFactory dateFormatFactory, LocaleProvider localeProvider,
		int poolSizePerLocale)
	{
		Check.checkNotNull(dateFormatFactory);
		Check.checkNotNull(localeProvider);

		this.dateFormatFactory = dateFormatFactory;
		this.localeProvider = localeProvider;
		this.poolSizePerLocale = poolSizePerLocale;
	}

	public String format(Date date, TimeZone tz, Locale locale)
	{
		DateFormat format = aquire(locale);
		try
		{
			if( tz == null )
			{
				return format.format(date);
			}
			else
			{
				// need to mutate the format...
				DateFormat clone = (DateFormat) format.clone();
				clone.setTimeZone(tz);
				return clone.format(date);
			}
		}
		finally
		{
			release(format, locale);
		}
	}

	public String format(Date date, TimeZone tz)
	{
		return format(date, tz, localeProvider.getCurrentLocale());
	}

	public Date parse(String date, TimeZone tz, Locale locale) throws ParseException
	{
		DateFormat format = aquire(locale);
		try
		{
			if( tz == null )
			{
				return format.parse(date);
			}
			else
			{
				// need to mutate the format...
				DateFormat clone = (DateFormat) format.clone();
				clone.setTimeZone(tz);
				return clone.parse(date);
			}
		}
		finally
		{
			release(format, locale);
		}
	}

	public Date parse(String date, TimeZone tz) throws ParseException
	{
		return parse(date, tz, localeProvider.getCurrentLocale());
	}

	private DateFormat aquire(Locale locale)
	{
		synchronized( mutex )
		{
			Deque<DateFormat> sp = getSubpool(locale);
			return !sp.isEmpty() ? sp.removeFirst() : dateFormatFactory.createDateFormat(locale);
		}
	}

	private void release(DateFormat format, Locale locale)
	{
		synchronized( mutex )
		{
			Deque<DateFormat> sp = getSubpool(locale);
			if( sp.size() >= poolSizePerLocale )
			{
				sp.addFirst(format);
			}
		}
	}

	/**
	 * Only to be called by methods holding the mutex lock
	 */
	private Deque<DateFormat> getSubpool(Locale locale)
	{
		if( pool == null )
		{
			pool = new HashMap<Locale, Deque<DateFormat>>(1);
		}

		Deque<DateFormat> sp = pool.get(locale);
		if( sp == null )
		{
			sp = new LinkedList<DateFormat>();
			pool.put(locale, sp);
		}

		return sp;
	}

	public interface DateFormatFactory
	{
		DateFormat createDateFormat(Locale locale);
	}

	public interface LocaleProvider
	{
		Locale getCurrentLocale();
	}

	public static class BasicDateFormatFactory implements DateFormatFactory
	{
		private final String format;

		public BasicDateFormatFactory(String format)
		{
			this.format = format;
		}

		@Override
		public DateFormat createDateFormat(Locale locale)
		{
			return new SimpleDateFormat(format, locale);
		}
	}
}
