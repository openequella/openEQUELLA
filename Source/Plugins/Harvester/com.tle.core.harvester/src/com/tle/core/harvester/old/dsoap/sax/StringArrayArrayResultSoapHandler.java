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

package com.tle.core.harvester.old.dsoap.sax;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

/**
 * @author adame
 */
public class StringArrayArrayResultSoapHandler extends DefaultSoapHandler
{
	private List results;
	private List subarray;

	@Override
	protected void hookStartDocument()
	{
		results = new ArrayList();
	}

	@Override
	protected void hookStartElement(String namespaceURL, String localName, String qname, Attributes attributes)
	{
		if( getDepth() == 4 )
		{
			subarray = new ArrayList();
			results.add(subarray);
		}
	}

	@Override
	protected void hookEndElement(String namespaceURL, String localName, String qname)
	{
		if( getDepth() == 5 )
		{
			subarray.add(getAcculumulator());
		}
	}

	public String[][] getStringArrayArrayResult()
	{
		int size = results.size();
		String[][] rv = new String[size][];
		for( int i = 0; i < size; ++i )
		{
			List list = (List) results.get(i);
			if( list != null )
			{
				rv[i] = getStringArray(list);
			}
		}
		return rv;
	}

	public String[] getStringArray(List list)
	{
		int size = list.size();
		String[] rv = new String[size];
		for( int i = 0; i < size; ++i )
		{
			rv[i] = (String) list.get(i);
		}
		return rv;
	}
}