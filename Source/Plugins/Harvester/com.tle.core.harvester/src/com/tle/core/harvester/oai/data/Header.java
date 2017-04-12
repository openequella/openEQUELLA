/*
 * Created on Apr 12, 2005
 */

package com.tle.core.harvester.oai.data;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class Header
{
	private String identifier;
	private String datestamp;
	private Collection specs;
	private String status;

	public Header()
	{
		//
	}

	public String getDatestamp()
	{
		return datestamp;
	}

	public void setDatestamp(String datestamp)
	{
		this.datestamp = datestamp;
	}

	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public void addSpec(String spec)
	{
		Collection specss = getSpecs();
		specss.add(spec);
	}

	public Collection getSpecs()
	{
		if( specs == null )
		{
			specs = new ArrayList();
		}
		return specs;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
