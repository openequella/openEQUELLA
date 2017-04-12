/*
 * Created on Jan 11, 2005
 */
package com.tle.core.harvester.old.dsoap.sax;

import java.util.ArrayList;
import java.util.List;

/**
 * @author adame
 */
public class IntArrayResultSoapHandler extends DefaultSoapHandler
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

	public int[] getIntArrayResult()
	{
		int size = results.size();
		int[] rv = new int[size];
		for( int i = 0; i < size; ++i )
		{
			rv[i] = Integer.parseInt((String) results.get(i));
		}
		return rv;
	}
}