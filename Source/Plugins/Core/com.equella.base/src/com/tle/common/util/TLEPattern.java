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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TLEPattern
{
	private TLEPattern()
	{
		// Nothing to see here, move along...
	}

	public static boolean matches(String pattern, String s)
	{
		return compile(pattern, true).matcher(s).matches();
	}

	public static List<String> matches(String p, Collection<?> list)
	{
		List<String> matches = new ArrayList<String>();
		Pattern pattern = compile(p, true);
		for( Object o : list )
		{
			String s = o.toString();
			Matcher m = pattern.matcher(s);
			if( m.matches() )
			{
				matches.add(s);
			}
		}
		return matches;
	}

	public static Pattern compile(String s)
	{
		return compile(s, 0);
	}

	public static Pattern compile(String s, boolean caseInsenstive)
	{
		return compile(s, caseInsenstive ? Pattern.CASE_INSENSITIVE : 0);
	}

	public static Pattern compile(String s, int flags)
	{
		return Pattern.compile(replaceAll(s), flags);
	}

	@SuppressWarnings("nls")
	private static String replaceAll(String s)
	{
		s = s.replaceAll("\\*", ".*");
		return s;
	}
}
