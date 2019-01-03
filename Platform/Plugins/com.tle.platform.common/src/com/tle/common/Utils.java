/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.tle.annotation.Nullable;

@SuppressWarnings("nls")
public class Utils
{
	public static final String CHARSET_ENCODING = "UTF-8";

	public static final String ACCESS_PATH = "/access/";
	public static final String SIGNON_PATH = "/signon.do";

	public static final String DASHBOARD_PAGE = "home.do";
	public static final String SEARCHING_PAGE = "searching.do";
	public static final String HIERARCHY_PAGE = "hierarchy.do";
	public static final String CLOUDSEARCH_PAGE = "cloudsearch.do";

	// TODO: extension point?
	public static final String DEFAULT_HOME_PAGE = "access/" + DASHBOARD_PAGE;

	public static String ent(String szStr)
	{
		if( Check.isEmpty(szStr) )
		{
			return "";
		}

		StringBuilder szOut = new StringBuilder();
		final char[] chars = szStr.toCharArray();
		for( final char ch : chars )
		{
			switch( ch )
			{
				case '<':
					szOut.append("&lt;");
					break;

				case '>':
					szOut.append("&gt;");
					break;

				case '&':
					szOut.append("&amp;");
					break;

				case '"':
					szOut.append("&quot;");
					break;

				default:
					// http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
					// regular displayable ASCII:
					if( ch == 0xA || ch == 0xD || ch == 0x9 || (ch >= 0x20 && ch <= 0x007F) )
					{
						szOut.append(ch);
					}
					else if( (ch > 0x007F && ch <= 0xD7FF) || (ch >= 0xE000 && ch <= 0xFFFD)
						|| (ch >= 0x10000 && ch <= 0x10FFFF) )
					{
						szOut.append("&#x");
						final String hexed = Integer.toHexString(ch);
						// wooo, unrolled loops
						switch( 4 - hexed.length() )
						{
							case 3:
							case 2:
							case 1:
								szOut.append('0');
								break;
							default:
								break;
						}
						szOut.append(hexed);
						szOut.append(';');
					}
					// else we discard the character entirely.
					// It CANNOT be placed in XML
					break;
			}
		}

		return szOut.toString();
	}

	public static String unent(String szStr)
	{
		StringBuilder out = new StringBuilder();
		StringBuilder ent = new StringBuilder();
		boolean inEnt = false;
		int len = szStr.length();
		for( int offs = 0; offs < len; offs++ )
		{
			char c = szStr.charAt(offs);
			if( inEnt )
			{
				if( (c == '#' || Character.isLetterOrDigit(c)) && ent.length() <= 6 )
				{
					ent.append(c);
					continue;
				}
				if( c == ';' )
				{
					inEnt = false;
					String szEnt = ent.toString().toLowerCase();
					if( szEnt.equals("nbsp") )
					{
						out.append('\240');
						continue;
					}
					if( szEnt.equals("quot") )
					{
						out.append('"');
						continue;
					}
					if( szEnt.equals("apos") )
					{
						out.append('\'');
						continue;
					}
					if( szEnt.equals("lt") )
					{
						out.append('<');
						continue;
					}
					if( szEnt.equals("gt") )
					{
						out.append('>');
						continue;
					}
					if( szEnt.equals("amp") )
					{
						out.append('&');
						continue;
					}
					if( szEnt.length() > 0 && szEnt.charAt(0) == '#' )
					{
						try
						{
							out.append((char) Integer.parseInt(szEnt.substring(1)));
						}
						catch( NumberFormatException e )
						{
							// Ignore
						}
					}
					else
					{
						out.append('&');
						out.append(ent);
						out.append(c);
					}
				}
				else
				{
					inEnt = false;
					out.append('&');
					out.append(ent);
					offs--;
				}
				continue;
			}
			if( c == '&' )
			{
				inEnt = true;
				ent.setLength(0);
			}
			else
			{
				out.append(c);
			}
		}

		if( inEnt )
		{
			out.append('&');
			out.append(ent);
		}
		return out.toString();
	}

	/**
	 * The (theoretical) opposite of String.split()
	 * 
	 * @param objects
	 * @param joiner
	 * @return
	 */
	public static String join(Object[] objects, String joiner)
	{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for( Object part : objects )
		{
			if( !first )
			{
				result.append(joiner);
			}
			result.append(part);
			first = false;
		}
		return result.toString();
	}

	/**
	 * Ensures start is a valid point within the string text. The end of the
	 * resulting string will be original end point of the source string
	 * 
	 * @param text
	 * @param start If negative, used as a position from the right of the string
	 * @return
	 */
	public static String safeSubstring(final String text, final int start)
	{
		if( text == null )
		{
			return null;
		}
		return safeSubstring(text, start, text.length());
	}

	/**
	 * Ensures start and end are valid points within the string text
	 * 
	 * @param text
	 * @param start If negative, used as a position from the right of the string
	 * @param end If negative, used as a position from the right of the string
	 * @return
	 */
	public static String safeSubstring(final String text, final int start, final int end)
	{
		if( text == null )
		{
			return null;
		}
		int newStart = start;
		int newEnd = end;
		int textLength = text.length();

		if( newStart < 0 )
		{
			// from the right
			newStart = textLength + newStart;

			// went too far
			if( newStart < 0 )
			{
				newStart = 0;
			}
		}

		if( newEnd > textLength )
		{
			newEnd = textLength;
		}
		else if( newEnd < 0 )
		{
			// from the right
			newEnd = textLength + newEnd;
		}
		if( newEnd < 0 )
		{
			newEnd = 0;
		}

		// ensure start not too big
		if( newStart > newEnd )
		{
			newStart = newEnd;
		}
		return text.substring(newStart, newEnd);
	}

	/**
	 * Accepts very loose definitions of true/false.
	 * <table>
	 * <tr>
	 * <td>1</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>yes</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>on</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>t</td>
	 * <td>true</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td>false</td>
	 * </tr>
	 * <tr>
	 * <td>no</td>
	 * <td>false</td>
	 * </tr>
	 * <tr>
	 * <td>off</td>
	 * <td>false</td>
	 * </tr>
	 * <tr>
	 * <td>f</td>
	 * <td>false</td>
	 * </tr>
	 * </table>
	 * Anything else will return defaultValue
	 * 
	 * @param s
	 * @param defaultValue
	 * @return
	 */
	public static boolean parseLooseBool(String s, boolean defaultValue)
	{
		if( s == null || s.trim().length() == 0 )
		{
			return defaultValue;
		}
		if( Boolean.TRUE.toString().equalsIgnoreCase(s) || "1".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s)
			|| "on".equalsIgnoreCase(s) || "t".equalsIgnoreCase(s) )
		{
			return true;
		}
		else if( Boolean.FALSE.toString().equalsIgnoreCase(s) || "0".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s)
			|| "off".equalsIgnoreCase(s) || "f".equalsIgnoreCase(s) )
		{
			return false;
		}
		else
		{
			return defaultValue;
		}
	}

	public static int parseInt(String s, int defaultInt)
	{
		if( Check.isEmpty(s) )
		{
			return defaultInt;
		}
		try
		{
			return Integer.parseInt(s);
		}
		catch( NumberFormatException e )
		{
			return defaultInt;
		}
	}

	/**
	 * Parse a boolean from a string, but have a default value if the string is
	 * not exactly either true or false. Boolean.parseBoolean(String) is very
	 * similar, but will always default to false if the string is not exactly
	 * "true".
	 */
	public static boolean parseBool(String s, boolean defaultValue)
	{
		if( Boolean.TRUE.toString().equalsIgnoreCase(s) )
		{
			return true;
		}
		else if( Boolean.FALSE.toString().equalsIgnoreCase(s) )
		{
			return false;
		}
		else
		{
			return defaultValue;
		}
	}

	/**
	 * @param text
	 * @param start
	 * @param caseCompare
	 * @return true if both null, or text.startsWith(start)
	 */
	public static boolean safeStartsWith(String text, String start, boolean caseCompare)
	{
		if( text == null && start == null )
		{
			return true;
		}
		if( text == null || start == null )
		{
			return false;
		}
		if( !caseCompare )
		{
			return text.toLowerCase().startsWith(start.toLowerCase());
		}
		return text.startsWith(start);
	}

	/**
	 * Capitalises the first letter of every word Eg "barry ballshank" becomes
	 * "Barry Ballshank" Note that it only uses spaces to tokenise the
	 * words...not any whitespace
	 * 
	 * @param text
	 * @return A capitalised version of text
	 */
	public static String capitaliseWords(String text)
	{
		StringBuilder result = new StringBuilder(text.length());
		String[] words = text.split(" ");
		for( String word : words )
		{
			result.append(Character.toUpperCase(word.charAt(0)));
			if( word.length() > 1 )
			{
				result.append(word.substring(1));
			}
			result.append(" ");
		}
		return result.toString();
	}

	/**
	 * Same as the SQL COALESCE. Will get first non empty value and return it.
	 * 
	 * @param strings Any number of strings
	 * @return The first non empty string
	 */
	public static String coalesce(String... strings)
	{
		for( String s : strings )
		{
			if( !Check.isEmpty(s) )
			{
				return s;
			}
		}
		return "";
	}

	private static void addHexValue(StringBuilder sbuf, int num)
	{
		String hex = Integer.toHexString(num);
		for( int len = hex.length(); len < 4; len++ )
		{
			sbuf.append('0');
		}
		sbuf.append(hex);
	}

	public static String jsescape(String szStr)
	{
		StringBuilder szOut = new StringBuilder();

		if( szStr != null )
		{
			final int count = szStr.length();
			for( int i = 0; i < count; i++ )
			{
				char ch = szStr.charAt(i);
				if( ch < 0x20 )
				{
					szOut.append("\\u");
					addHexValue(szOut, ch);
				}
				else
				{
					switch( ch )
					{
						case '\\':
							szOut.append("\\\\");
							break;
						case '\'':
						case '"':
							szOut.append("\\u");
							addHexValue(szOut, ch);
							break;
						default:
							szOut.append(ch);
					}
				}
			}
		}
		return szOut.toString();
	}

	public static String formatDuration(long duration)
	{
		if( duration < TimeUnit.MINUTES.toSeconds(1) )
		{
			return Long.toString(duration);
		}
		// TimeUnits truncates extra seconds, aka, floor()
		long hours = TimeUnit.SECONDS.toHours(duration);
		long minutes = TimeUnit.SECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours);
		long seconds = duration - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(minutes);

		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

	/**
	 * On a parse error, re-throw
	 * 
	 * @param dateAsString
	 * @return A UTC Date
	 */
	@Nullable
	public static Date parseDate(@Nullable String dateAsString, SimpleDateFormat dateFormat) throws ParseException
	{
		if( Check.isEmpty(dateAsString) )
		{
			return null;
		}
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.parse(dateAsString);
	}

	/**
	 * On a parse error, re-throw
	 * 
	 * @param java.util.Date
	 * @return dateAsString
	 */
	public static String formatDateToPlainString(@Nullable Date date, SimpleDateFormat dateFormat)
	{
		if( date == null )
		{
			return null;
		}
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(date);
	}
}
