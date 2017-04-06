/*
 * Created on Apr 14, 2005
 */
package com.tle.core.harvester.oai.data;

import java.util.ArrayList;

/**
 *
 */
public class List extends ArrayList<Object>
{
	private static final long serialVersionUID = 1L;

	private ResumptionToken token;

	@Override
	public boolean add(Object o)
	{
		if( o instanceof ResumptionToken )
		{
			token = (ResumptionToken) o;
		}
		else
		{
			return super.add(o);
		}
		return true;
	}

	public ResumptionToken getResumptionToken()
	{
		return token;
	}

	public void setResumptionToken(ResumptionToken token)
	{
		this.token = token;
	}
}
