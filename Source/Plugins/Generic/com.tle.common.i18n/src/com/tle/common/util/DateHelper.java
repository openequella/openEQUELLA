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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Utils;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
public final class DateHelper
{
	public static final String UTC_TIMEZONE_ID = "Etc/UTC";
	public static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone(UTC_TIMEZONE_ID);

	public static UtcDate now()
	{
		return new UtcDate();
	}

	public static LocalDate now(TimeZone zone)
	{
		return new LocalDate(new UtcDate(), zone);
	}

	@Nullable
	public static Date parseOrNullDate(String date, Dates dateFormat)
	{
		if( Check.isEmpty(date) )
		{
			return null;
		}
		try
		{
			return new UtcDate(date, dateFormat).toDate();
		}
		catch( ParseException e )
		{
			return null;
		}
	}

	@Nullable
	public static UtcDate parseOrNull(String date, Dates dateFormat)
	{
		Date d = dateFormat.parseOrNull(date, UTC_TIMEZONE);
		return (d == null ? null : new UtcDate(d));
	}

	@Nullable
	public static LocalDate parseOrNull(String date, Dates dateFormat, TimeZone zone)
	{
		Date d = dateFormat.parseOrNull(date, zone);
		return (d == null ? null : new LocalDate(d, zone));
	}

	public static List<String> getTimeZoneIds()
	{
		List<String> zoneIds = new ArrayList<String>();

		// remove zoneIds that are <= 3 chars long, otherwise you get all sorts
		// of undesirable rubbish
		// this seems to be the best way to weed them out.
		for( String zoneId : TimeZone.getAvailableIDs() )
		{
			if( zoneId.length() > 3 )
			{
				zoneIds.add(zoneId);
			}
		}

		return zoneIds;
	}

	public static List<NameValue> getTimeZoneNameValues(@Nullable NameValue defaultEntry, boolean includeUTCAtTop)
	{
		final List<NameValue> zones = Lists.newArrayList(Lists.transform(getTimeZoneIds(),
			new Function<String, NameValue>()
			{
				@Override
				public NameValue apply(String zone)
				{
					TimeZone tzone = TimeZone.getTimeZone(zone);
					String longName = tzone.getDisplayName(true, TimeZone.SHORT);
					int rawOff = tzone.getRawOffset();
					String gmtTZ = String.format("(UTC%s%02d:%02d) ", rawOff < 0 ? "-" : "+",
						Math.abs(rawOff) / 3600000, Math.abs(rawOff) / 60000 % 60);
					return new NameValue(gmtTZ + zone + " - " + longName, zone);
				}
			}));
		Collections.sort(zones, new Comparator<NameValue>()
		{
			@Override
			public int compare(NameValue o1, NameValue o2)
			{
				final String fullname1 = o1.getName();
				final String fullname2 = o2.getName();
				// sort by number first
				final int n1 = Integer.parseInt(Utils.safeSubstring(fullname1, 4, 10).replace(":", ""));
				final int n2 = Integer.parseInt(Utils.safeSubstring(fullname2, 4, 10).replace(":", ""));
				if( n1 > n2 )
				{
					return 1;
				}
				else if( n1 < n2 )
				{
					return -1;
				}
				// Same zone, sort by name
				String name1 = Utils.safeSubstring(fullname1, 10);
				String name2 = Utils.safeSubstring(fullname2, 10);
				return name1.compareTo(name2);
			}
		});
		if( includeUTCAtTop )
		{
			zones.add(0, new NameValue("UTC", UTC_TIMEZONE_ID));
		}
		if( defaultEntry != null )
		{
			zones.add(0, defaultEntry);
		}
		return zones;
	}

	private DateHelper()
	{
		throw new Error();
	}
}
