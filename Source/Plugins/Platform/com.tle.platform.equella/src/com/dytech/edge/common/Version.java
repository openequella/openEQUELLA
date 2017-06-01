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

package com.dytech.edge.common;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.Random;

import com.tle.common.Check;

@SuppressWarnings("nls")
public final class Version
{
	private final String mmr; // MMR = major.minor.revision
	private final String display;
	private final String commit;
	private boolean dev;

	public static Version load()
	{
		return load((Class<?>) null);
	}

	public static Version load(Class<?> klass)
	{
		String f = "version.properties";
		if( klass == null )
		{
			klass = Version.class;
			f = '/' + f;
		}

		try( InputStream in = klass.getResourceAsStream(f) )
		{
			Properties p = null;
			if( in != null )
			{
				p = new Properties();
				p.load(in);
			}
			return load(p);
		}
		catch( IOException ex )
		{
			throw new RuntimeException("Error attempting to load version.properties");
		}
	}

	public static Version load(Properties p)
	{
		if( p == null )
		{
			return new Version("dev" + new Random().nextInt(1000), "Development", "dev (dev)", true);
		}
		else
		{
			return new Version(p.getProperty("version.mmr"), p.getProperty("version.display"),
				p.getProperty("version.commit"), false);
		}
	}

	private Version(String mmr, String display, String commit, boolean dev)
	{
		this.mmr = mmr;
		this.display = display;
		this.commit = commit;
		this.dev = dev;
	}

	public boolean isDevelopment()
	{
		return dev;
	}

	public String getCommit()
	{
		return commit;
	}

	public String getMmr()
	{
		return mmr;
	}

	public String getDisplay()
	{
		return display;
	}

	public String getFull()
	{
		return MessageFormat.format("{0} ({1})", mmr, display);
	}

	/**
	 * Both this version and the parameter are evaluated as doubles, to compare
	 * the major.minor number (eg 4.2, 5.3 etc) to determine if a license is
	 * trumped by the running system (assuming this.version to be the running
	 * system and the other to be the licence). If the parameter is empty, we
	 * determine yes (any actual version trumps a no-show. If the parameter is
	 * non-digital it trumps any running version number so we return false.
	 * 
	 * @param otherVersion
	 * @return true if this.version greater the other, otherwise false.
	 */
	public boolean greaterVersionThan(String otherVersion)
	{
		// no actual licence value presented? This.version is greater.
		if( Check.isEmpty(otherVersion) )
		{
			return true;
		}

		otherVersion = otherVersion.trim();

		// Non-digit licence value? License.DEVELOPMENT_BUILD trumps
		// this.version.
		// Any other non-numeric value is deemed lower than this.version.
		if( otherVersion.startsWith("dev") )
		{
			return false;
		}

		// Empty or non-digit this.version? Allow anything. Either way return
		// false.
		if( Check.isEmpty(mmr) || !Character.isDigit(mmr.charAt(0)) )
		{
			return false;
		}

		return getMajorMinorValue(mmr) > getMajorMinorValue(otherVersion);
	}

	/**
	 * A tolerant double parser, doesn't throw any exceptions or complain.
	 * Unparsable strings returned as zero. Negatives not considered (hence a
	 * leading '-' renders the string unparsable.<br>
	 * From a string with an arbitrary content, extract the numeric value
	 * including the decimal part beyond (any) decimal point. Stop parsing once
	 * second point or non-digit found, so from 5.01.Alpha6 we get a double:
	 * 5.01
	 * 
	 * @param str
	 * @return
	 */
	public static double getMajorMinorValue(String str)
	{
		double dbl = 0;
		boolean decimalPoint = false;
		int decimalIndex = 1;
		for( int i = 0; i < str.length(); ++i )
		{
			if( !decimalPoint && str.charAt(i) == '.' )
			{
				decimalPoint = true;
				continue;
			}
			if( Character.isDigit(str.charAt(i)) )
			{
				int charDigit = Character.getNumericValue(str.charAt(i));
				if( !decimalPoint )
				{
					dbl = (dbl * 10) + charDigit;
				}
				else
				{
					dbl += charDigit / Math.pow(10, decimalIndex);
					decimalIndex++;
				}
			}
			else
			{
				break;
			}
		}
		return dbl;
	}
}
