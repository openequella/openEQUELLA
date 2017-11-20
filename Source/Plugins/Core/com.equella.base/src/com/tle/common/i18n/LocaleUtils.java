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

package com.tle.common.i18n;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
public final class LocaleUtils
{
	private static final Pattern LOCALE_REGEX = Pattern.compile("^([a-z][a-z])?(?:_([A-Z][A-Z])?(?:_(\\w+))?)?$"); //$NON-NLS-1$

	@SuppressWarnings("nls")
	public static String toHtmlLang(Locale locale)
	{
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();
		return lang + (!Check.isEmpty(country) ? '-' + country : "") + (!Check.isEmpty(variant) ? '-' + variant : "");
	}

	/**
	 * From the supplied list of locales, find the closest one to the supplies
	 * locale
	 * 
	 * @param locales A collection possible locales to choose from
	 * @param locale The locale you would like to match
	 * @return The closest matching locale, or just the first available locale
	 *         if no match
	 */
	public static Locale getClosestLocale(Collection<Locale> locales, Locale locale)
	{
		Map<String, Locale> possibleMap = new LinkedHashMap<String, Locale>();
		for( Locale l : locales )
		{
			possibleMap.put(getExactKey(l), l);
		}
		return getClosestObjectForLocale(possibleMap, locale);
	}

	/**
	 * Retrieves the closest matching value from a map of Locale strings to
	 * values of some type.
	 */
	public static <T> T getClosestObjectForLocale(Map<String, T> values, Locale locale)
	{
		Stack<String> keys = getAllPossibleKeys(locale);
		while( !keys.isEmpty() )
		{
			String key = keys.pop();
			T value = values.get(key);
			if( value != null )
			{
				return value;
			}
		}

		Collection<T> vs = values.values();
		return vs.isEmpty() ? null : vs.iterator().next();
	}

	/**
	 * Parses a locale from a string.
	 */
	public static Locale parseLocale(String localeString)
	{
		if( localeString != null )
		{
			Matcher m = LOCALE_REGEX.matcher(localeString.trim());
			if( m.matches() )
			{
				return new Locale(Check.nullToEmpty(m.group(1)), Check.nullToEmpty(m.group(2)), Check.nullToEmpty(m
					.group(3)));
			}
		}
		throw new RuntimeException("Error parsing locale: " + localeString);
	}

	/**
	 * Determines the priority for a LanaguageString based on a locale.
	 */
	public static int getPriorityForLocale(Locale locale)
	{
		if( Check.isEmpty(locale.getLanguage()) )
		{
			return 0;
		}
		else if( Check.isEmpty(locale.getCountry()) )
		{
			return 1;
		}
		else if( Check.isEmpty(locale.getVariant()) )
		{
			return 2;
		}
		else
		{
			return 3;
		}
	}

	/**
	 * Returns the exact locale string.
	 */
	public static String getExactKey(Locale locale)
	{
		return getKeys(new KeyGatherer<String>()
		{
			@Override
			void addKey(String part)
			{
				addToBuffer(part);
			}

			@Override
			public String getResult()
			{
				return buffer.toString();
			}
		}, locale);
	}

	/**
	 * Returns a stack of locale strings, with the most specific locale on top.
	 */
	public static Stack<String> getAllPossibleKeys(Locale locale)
	{
		return getKeys(new KeyGatherer<Stack<String>>()
		{
			Stack<String> result = new Stack<String>();

			@Override
			void addKey(String part)
			{
				addToBuffer(part);
				result.push(buffer.toString());
			}

			@Override
			Stack<String> getResult()
			{
				return result;
			}
		}, locale);
	}

	/**
	 * Transforms a locale to possible key strings, and passes them to a key
	 * gatherer.
	 */
	private static <T> T getKeys(KeyGatherer<T> gatherer, Locale locale)
	{
		return getKeys(gatherer, locale.getLanguage(), locale.getCountry(), locale.getVariant());
	}

	/**
	 * Transforms the given locale elements to possible key strings, and passes
	 * them to the gatherer.
	 */
	private static <T> T getKeys(KeyGatherer<T> gatherer, String language, String country, String variant)
	{
		boolean l = !Check.isEmpty(language);
		boolean c = !Check.isEmpty(country);
		boolean v = !Check.isEmpty(variant);

		gatherer.addKey(language);

		if( c || (l && v) )
		{
			gatherer.addKey(country); // This may just append '_'
		}

		if( v && (l || c) )
		{
			gatherer.addKey(variant);
		}

		return gatherer.getResult();
	}

	protected abstract static class KeyGatherer<T>
	{
		protected StringBuilder buffer = new StringBuilder(15);

		protected void addToBuffer(String part)
		{
			if( buffer.length() > 0 )
			{
				buffer.append('_');
			}
			buffer.append(part);
		}

		abstract void addKey(String part);

		abstract T getResult();
	}

	private LocaleUtils()
	{
		throw new Error();
	}
}
