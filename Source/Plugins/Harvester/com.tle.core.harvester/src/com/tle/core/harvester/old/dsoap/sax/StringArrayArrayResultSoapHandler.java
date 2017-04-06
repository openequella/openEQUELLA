/*
 * Created on Jan 11, 2005
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