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

package com.tle.core.activation.validation;

import java.util.BitSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PageCounter
{
	private static final Pattern REGEX_ANUMBER = Pattern.compile("[0-9ivxlcdmIVXLCDM]+"); //$NON-NLS-1$
	private static final Pattern REGEX_SQUARE = Pattern.compile("(.*)\\[(.*?)([0-9ivxlcdmIVXLCDM]*)\\]"); //$NON-NLS-1$

	// .compile("[\\[([\\\\divxlcdmIVXLCDM]*)\\]]");

	private PageCounter()
	{
		throw new Error();
	}

	@SuppressWarnings("nls")
	public static int countTotalPages(String pages)
	{
		int total = 0;
		if( pages != null )
		{
			// remove everything in round brackets
			pages = pages.replaceAll("\\([^\\)]*\\)", "");

			// remove everything after the first colon, semicolon or plus sign
			int ind = pages.indexOf(':');
			if( ind >= 0 )
			{
				pages = pages.substring(0, ind);
			}

			ind = pages.indexOf(';');
			if( ind >= 0 )
			{
				pages = pages.substring(0, ind);
			}

			ind = pages.indexOf('+');
			if( ind >= 0 )
			{
				pages = pages.substring(0, ind);
			}

			// replace all spaces
			pages = pages.replaceAll("\\s+", "");

			// remove nv.
			pages = pages.replaceAll("\\[\\d+\\]v\\.", "");

			// remove ca.
			pages = pages.replace("ca.", "");

			// remove P.
			pages = pages.replace("p.", "");

			// remove p
			pages = pages.replace("p", "");

			// remove leaves
			pages = pages.replaceAll("[Ll]eaves", "");

			String[] array = pages.split(",");
			for( String range : array )
			{
				range = range.trim();
				Matcher matcher = REGEX_SQUARE.matcher(range);
				boolean matched = matcher.matches();
				if( matched )
				{
					String firstBit = matcher.group(1);
					String ieBit = matcher.group(2);
					if( firstBit.isEmpty() || ieBit.equalsIgnoreCase("i.e.") )
					{
						range = matcher.group(3);
					}
					else if( REGEX_ANUMBER.matcher(firstBit).matches() )
					{
						range = firstBit;
					}
				}
				if( REGEX_ANUMBER.matcher(range).matches() )
				{
					total += parseNumber(range);
				}
			}
		}
		return total;
	}

	private static int parseNumber(String string)
	{
		return parseNumber(string, null);
	}

	private static int parseNumber(String string, Boolean[] type)
	{
		char chars[] = string.toLowerCase().toCharArray();
		StringBuilder sbuf = new StringBuilder();
		boolean inDigits = false;
		boolean inRoman = false;
		for( char ch : chars )
		{
			boolean digit = Character.isDigit(ch);
			boolean roman = !digit
				&& (ch == 'm' || ch == 'd' || ch == 'c' || ch == 'l' || ch == 'x' || ch == 'v' || ch == 'i');
			if( (inDigits && !digit) || (inRoman && !roman) )
			{
				break;
			}
			inDigits = digit;
			inRoman = roman;
			if( inDigits || inRoman )
			{
				sbuf.append(ch);
			}
		}
		string = sbuf.toString();
		int ret = 0;
		if( inDigits )
		{
			ret = Integer.parseInt(string);
		}
		if( inRoman )
		{
			ret = (int) RomanFormat.toLong(string);
		}
		if( type != null )
		{
			type[0] = inDigits ? Boolean.TRUE : Boolean.FALSE;
		}
		return ret;
	}

	public static int countTotalRange(String range)
	{
		RangeCounter counter = new RangeCounter();
		processRange(range, counter);
		return counter.getTotal();
	}

	public static void processRange(String pages, RangeCounter counter)
	{
		if( pages != null )
		{
			String[] array = pages.split("[,&]"); //$NON-NLS-1$
			for( String range : array )
			{
				range = range.trim();
				if( range.length() == 0 )
				{
					continue;
				}
				boolean inSquare = false;
				int sqStart = range.indexOf('[');
				if( sqStart != -1 )
				{
					int sqEnd = range.indexOf(']', sqStart);
					if( sqEnd != -1 )
					{
						range = range.substring(sqStart + 1, sqEnd);
						inSquare = true;
					}
				}
				int index = range.indexOf('-');
				if( index >= 0 )
				{
					String first = range.substring(0, index);
					String second = range.substring(index + 1);
					counter.processRange(first, second, inSquare);
				}
				else
				{
					counter.processRange(range, null, inSquare);
				}
			}
		}
	}

	public static class RangeCounter
	{
		private final BitSet decimals = new BitSet();
		private final BitSet roman = new BitSet();
		private int extras;

		public void processRange(String start, String end, boolean inSquare)
		{
			try
			{
				Boolean[] isDec = new Boolean[1];
				int starti = parseNumber(start, isDec);
				BitSet whichBits = isDec[0] ? decimals : roman;
				if( end != null )
				{
					int endi = parseNumber(end);
					if( endi > 100000 || endi <= starti )
					{
						// do not process
						return;
					}
					whichBits.set(starti, endi + 1);
				}
				else if( starti < 100000 )
				{
					if( inSquare )
					{
						extras += starti;
					}
					else
					{
						whichBits.set(starti);
					}
				}
			}
			catch( NumberFormatException nfe )
			{
				// we'd prefer not to do anything
			}
		}

		public int getTotal()
		{
			return decimals.cardinality() + roman.cardinality() + extras;
		}

		public String getRangeEnd()
		{
			int lastBit = decimals.length();
			if( lastBit != 0 )
			{
				return Integer.toString(lastBit - 1);
			}
			lastBit = roman.length();
			if( lastBit != 0 )
			{
				return new RomanFormat().format(lastBit - 1);
			}
			return null;
		}

		public String getRangeStart()
		{
			int firstBit = decimals.nextSetBit(0);
			if( firstBit != -1 )
			{
				return Integer.toString(firstBit);
			}
			firstBit = roman.nextSetBit(0);
			if( firstBit != -1 )
			{
				return new RomanFormat().format(firstBit);
			}
			return null;
		}
	}
}
