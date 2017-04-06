package com.tle.core.fedsearch.impl;

import java.util.Collection;

import com.dytech.devlib.PropBagEx;
import com.tle.core.fedsearch.GenericRecord;

/**
 * @author aholland
 */
public class NullRecord implements GenericRecord
{
	@Override
	public Collection<String> getAuthors()
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public String getIsbn()
	{
		return null;
	}

	@Override
	public String getIssn()
	{
		return null;
	}

	@Override
	public String getLccn()
	{
		return null;
	}

	@Override
	public String getPhysicalDescription()
	{
		return null;
	}

	@Override
	public String getTitle()
	{
		return null;
	}

	@Override
	public String getType()
	{
		return null;
	}

	@Override
	public String getUri()
	{
		return null;
	}

	@Override
	public String getUrl()
	{
		return null;
	}

	@Override
	public PropBagEx getXml()
	{
		return new PropBagEx();
	}
}
