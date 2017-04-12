/*
 * Created on Jan 11, 2005
 */
package com.tle.core.harvester.old.dsoap.sax;

/**
 * @author adame
 */
public class StringResultSoapHandler extends DefaultSoapHandler
{
	protected String result;

	@Override
	protected void hookStartDocument()
	{
		result = null;
	}

	@Override
	protected void hookEndElement(String namespaceURL, String localName, String qname)
	{
		if( getDepth() == 3 )
		{
			result = getAcculumulator();
		}
	}

	public String getStringResult()
	{
		return result;
	}
}