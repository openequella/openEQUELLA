/*
 * Created on Apr 11, 2005
 */

package com.tle.core.harvester.oai.data;

/**
 * 
 */
public class Record
{
	private Header header;
	private Object metadata;

	public Header getHeader()
	{
		return header;
	}

	public void setHeader(Header header)
	{
		this.header = header;
	}

	public Object getMetadata()
	{
		return metadata;
	}

	public void setMetadata(Object metadata)
	{
		this.metadata = metadata;
	}
}
