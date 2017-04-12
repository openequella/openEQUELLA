/*
 * Created on 14/03/2006
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
