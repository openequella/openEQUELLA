/*
 * Created on Jun 7, 2005
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
