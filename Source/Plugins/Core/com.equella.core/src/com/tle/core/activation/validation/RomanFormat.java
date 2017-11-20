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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * This NumberFormat converts long integers to and from Roman Numeral notation.
 * Once an instance has been created, the format and parse methods may be used
 * as defined in java.text.NumberFormat.
 * </p>
 * <p>
 * The <a
 * href="../benno/applets/RomanNumeralConverter.html">RomanNumeralConverter
 * applet</a> demonstrates the use of this class.
 * </p>
 * 
 * @see benno.applets.RomanNumeralConverter
 * @author Ben Clifford
 * @version $Revision: 1.11 $
 */
public class RomanFormat extends NumberFormat
{
	private static final long serialVersionUID = 1L;

	/**
	 * This method returns null. I have found no meaningful translation of
	 * floating point numbers to Roman numerals, however the NumberFormat method
	 * requires that it is implemented. Perhaps should just cast the double to a
	 * long and format accordingly.
	 */

	@Override
	public StringBuffer format(double n, StringBuffer a, FieldPosition p)
	{
		return null;
	}

	/**
	 * This nested class is used to map Roman symbols onto their numerical
	 * values. Used in the Roman class.
	 * 
	 * @see benno.applets.Roman
	 */

	public static class SymTab
	{
		/** Roman symbol */
		char symbol;

		/** Numerical value */
		long value;

		/**
		 * Constructor to build a SymTab from supplied symbol and value
		 * 
		 * @param s Roman symbol
		 * @param v Numerical value
		 */
		public SymTab(char s, long v)
		{
			this.symbol = s;
			this.value = v;
		}
	}

	/**
	 * This table maps individual Roman symbols onto their numerical values.<br>
	 * Unfortunately, JavaDoc JDK 1.1 does not create documentation for the
	 * inner class Roman.SymTab, so the reader cannot see the definition.
	 */

	protected static final List<RomanFormat.SymTab> syms = Collections.unmodifiableList(Arrays.asList(
		new RomanFormat.SymTab('M', 1000), new RomanFormat.SymTab('D', 500), new RomanFormat.SymTab('C', 100),
		new RomanFormat.SymTab('L', 50), new RomanFormat.SymTab('X', 10), new RomanFormat.SymTab('V', 5),
		new RomanFormat.SymTab('I', 1)));

	/**
	 * This method converts a Roman Numeral string to a long integer. It does
	 * not check that the string is in the correct format - for some incorrectly
	 * formatted numbers, i.e. iix, it will produce a number. For others, it
	 * will throw an exception.
	 * 
	 * @param s string of Roman Numerals
	 * @param parsePosition the place to start parsing
	 * @return A Long object containing the parsed Roman numeral
	 */

	@Override
	public Number parse(String text, ParsePosition parsePosition)
	{

		String s = text.substring(parsePosition.getIndex());

		long tot = 0, max = 0;
		char ch[] = s.toUpperCase().toCharArray();
		int i, p;
		for( p = ch.length - 1; p >= 0; p-- )
		{
			for( i = 0; i < syms.size(); i++ )
			{
				if( syms.get(i).symbol == ch[p] )
				{
					if( syms.get(i).value >= max )
					{
						max = syms.get(i).value;
						tot += max;
					}
					else
					{
						tot -= syms.get(i).value;
					}
				}
			}
		}

		// say that we parsed the whole string
		parsePosition.setIndex(s.length());
		return tot;
	}

	/**
	 * This method converts a Roman Numeral string to a long integer. It does
	 * not check that the string is in the correct format - for some incorrectly
	 * formatted numbers, i.e. iix, it will produce a number. For others, it
	 * will throw an exception.
	 * 
	 * @param s string of Roman Numerals
	 * @return The integer representation of the Numerals
	 */
	public static long toLong(String s)
	{
		long tot = 0, max = 0;
		char ch[] = s.toUpperCase().toCharArray();
		int i, p;
		for( p = ch.length - 1; p >= 0; p-- )
		{
			for( i = 0; i < syms.size(); i++ )
			{
				if( syms.get(i).symbol == ch[p] )
				{
					if( syms.get(i).value >= max )
					{
						max = syms.get(i).value;
						tot += max;
					}
					else
					{
						tot -= syms.get(i).value;
					}
				}
			}
		}
		return tot;
	}

	/**
	 * This method converts the supplied long into capitalised Roman numerals.<br>
	 * BUG: the method does not take account of the <code>FieldPosition p</code>
	 * parameter.
	 * 
	 * @param n The number to be converted into Roman numerals
	 * @param s The StringBuffer into which the output is to be placed.
	 * @return The StringBuffer s
	 */
	@Override
	public StringBuffer format(long n, StringBuffer s, FieldPosition p)
	{
		int i;
		while( n > 0 )
		{
			for( i = 0; i < syms.size(); i++ )
			{
				if( syms.get(i).value <= n )
				{
					int shift = i + (i % 2);
					if( i > 0 && shift < syms.size() && (syms.get(i - 1).value - syms.get(shift).value) <= n )
					{
						s.append(syms.get(shift).symbol);
						s.append(syms.get(i - 1).symbol);
						n = n - syms.get(i - 1).value + syms.get(shift).value;

						i = -1;
					}
					else
					{
						s.append(syms.get(i).symbol);
						n -= syms.get(i).value;
						i = -1;
					}
				}
			}
		}
		return s;
	}

	/**
	 * This method converts a long integer to capitalised Roman notation.
	 * 
	 * @param n The integer to convert to Roman Numerals.
	 * @return A String object containing the Roman Numerals.
	 */
	public static String toRoman(long n)
	{
		int i;
		StringBuilder s = new StringBuilder();
		while( n > 0 )
		{
			for( i = 0; i < syms.size(); i++ )
			{
				if( syms.get(i).value <= n )
				{
					int shift = i + (i % 2);
					if( i > 0 && shift < syms.size() && (syms.get(i - 1).value - syms.get(shift).value) <= n )
					{
						s.append(syms.get(shift).symbol);
						s.append(syms.get(i - 1).symbol);
						n = n - syms.get(i - 1).value + syms.get(shift).value;

						i = -1;

					}
					else
					{
						s.append(syms.get(i).symbol);
						n -= syms.get(i).value;
						i = -1;
					}
				}
			}
		}
		return s.toString();
	}
}