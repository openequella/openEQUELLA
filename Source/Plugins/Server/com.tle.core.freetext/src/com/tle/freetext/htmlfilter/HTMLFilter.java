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

package com.tle.freetext.htmlfilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.dytech.common.io.UnicodeReader;

/**
 * @author jmaginnis
 */
public class HTMLFilter
{

	/** Creates a new instance of HTMLFilter */
	Reader m_input;

	public HTMLFilter(InputStream inp)
	{
		m_input = new UnicodeReader(inp, "UTF-8");
	}

	public String getSummary(int nSize) throws IOException
	{
		StringBuilder out = new StringBuilder();
		StringBuilder tag = new StringBuilder();
		StringBuilder ent = new StringBuilder();
		boolean inTag = false;
		boolean inEnt = false;
		boolean inIgnore = false;
		boolean inTagName = false;
		boolean lastSpace = false;

		while( nSize > 0 )
		{
			int b = m_input.read();
			if( b == -1 )
			{
				break;
			}
			char c = (char) b;
			if( inEnt )
			{

				if( c == '#' || Character.isLetterOrDigit(c) )
				{
					ent.append(c);
				}
				else
				{
					inEnt = false;
					String szEnt = ent.toString().toLowerCase();
					if( szEnt.length() == 0 )
					{
						ent.append("&");
					}
					else if( szEnt.equals("nbsp") )
					{
						ent.append(' ');
					}
					else if( szEnt.equals("quot") )
					{
						ent.append('"');
					}
					else if( szEnt.equals("apos") )
					{
						ent.append('\'');
					}
					else if( szEnt.equals("lt") )
					{
						ent.append('<');
					}
					else if( szEnt.equals("gt") )
					{
						ent.append('>');
					}
					else if( szEnt.equals("amp") )
					{
						ent.append('&');
					}
					else if( szEnt.charAt(0) == '#' )
					{
						try
						{
							ent.append((char) Integer.parseInt(szEnt.substring(1)));
						}
						catch( NumberFormatException e )
						{
							ent.append('?');
						}
					}
					nSize--;
				}
			}
			else
			{
				if( !inTag )
				{
					if( c == '\t' )
					{
						c = ' ';
					}
					if( c == '\n' )
					{
						c = ' ';
					}
					if( c == '\r' )
					{
						continue;
					}
					if( c == '<' )
					{
						tag.setLength(0);
						inTagName = true;
						inTag = true;
					}
					else if( c == '&' )
					{
						ent.setLength(0);
						inEnt = true;
					}
					else if( !inIgnore )
					{
						if( c == ' ' && lastSpace )
						{
							continue;
						}
						if( c == ' ' && !lastSpace )
						{
							lastSpace = true;
						}
						else
						{
							lastSpace = false;
						}
						out.append(c);
						nSize--;
					}
				}
				else
				{
					if( inTagName )
					{
						if( c == '/' || Character.isLetterOrDigit(c) )
						{
							tag.append(c);
						}
						else
						{
							inTagName = false;
						}
					}
					if( c == '>' )
					{
						inTag = false;
						String szTag = tag.toString().toLowerCase();
						if( szTag.length() > 0 )
						{
							// System.out.println("TAG:" + szTag);
							if( szTag.equals("script") )
							{
								inIgnore = true;
							}
							if( szTag.equals("/script") )
							{
								inIgnore = false;
							}
						}
						if( lastSpace )
						{
							continue;
						}
						if( !lastSpace )
						{
							lastSpace = true;
						}
						else
						{
							lastSpace = false;
						}
						out.append(' ');
						nSize--;
					}
				}
			}
		}
		return out.toString();
	}

}
