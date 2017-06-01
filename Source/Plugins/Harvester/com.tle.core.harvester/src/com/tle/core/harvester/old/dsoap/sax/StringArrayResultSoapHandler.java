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

/**
 * @author adame
 */
public class StringArrayResultSoapHandler extends DefaultSoapHandler
{
	private List results;

	@Override
	protected void hookStartDocument()
	{
		results = new ArrayList();
	}

	@Override
	protected void hookEndElement(String namespaceURL, String localName, String qname)
	{
		if( (getDepth() == 4) )
		{
			results.add(getAcculumulator());
		}
	}

	public String[] getStringArrayResult()
	{
		int size = results.size();
		String[] rv = new String[size];
		for( int i = 0; i < size; ++i )
		{
			rv[i] = (String) results.get(i);
		}
		return rv;
	}
}