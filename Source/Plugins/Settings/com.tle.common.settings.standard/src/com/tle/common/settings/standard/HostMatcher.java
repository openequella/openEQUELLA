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

package com.tle.common.settings.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author jmaginnis
 */
public class HostMatcher
{
	private List<List<String>> matches;

	public HostMatcher(List<String> expressions)
	{
		matches = new ArrayList<List<String>>(expressions.size());

		for( String expr : expressions )
		{
			StringTokenizer stok = new StringTokenizer(expr, "."); //$NON-NLS-1$
			List<String> ips = new ArrayList<String>(4);
			while( stok.hasMoreTokens() )
			{
				ips.add(stok.nextToken());
			}
			matches.add(ips);
		}
	}

	public boolean matches(String host)
	{
		nextmatch : for( List<String> match : matches )
		{
			StringTokenizer stok = new StringTokenizer(host, "."); //$NON-NLS-1$
			int j = 0;
			while( stok.hasMoreTokens() )
			{
				String ip = stok.nextToken();
				String tomatch = match.get(j++);
				if( tomatch.equals("*") ) //$NON-NLS-1$
				{
					continue;
				}

				if( !tomatch.equals(ip) )
				{
					continue nextmatch;
				}
			}
			return true;
		}
		return false;
	}
}
