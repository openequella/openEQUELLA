/*
 * Copyright (c) 2011, EQUELLA All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of EQUELLA nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.util;

/**
 * A simple string utility class
 */
public abstract class StringUtils
{
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
			if( !isEmpty(s) )
			{
				return s;
			}
		}
		return "";
	}

	/**
	 * The (theoretical) opposite of String.split()
	 * 
	 * @param joiner Joining text, e.g. ","
	 * @param objects Stringable objects e.g. ["cat", "dog", "fish"]
	 * @return The joined text, e.g. "cat,dog,fish"
	 */
	public static String join(Object[] objects, String joiner)
	{
		final StringBuilder result = new StringBuilder();
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
	 * @param s
	 * @return true if s is null, empty or purely whitespace
	 */
	public static boolean isEmpty(String s)
	{
		return s == null || s.trim().length() == 0;
	}
}
