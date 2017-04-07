package com.tle.web.sections.js.generic;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.ElementId;

public class SimpleElementId implements ElementId
{
	private String id;
	private boolean used;

	public SimpleElementId(String id)
	{
		this.id = id;
	}

	@Override
	public String getElementId(SectionInfo info)
	{
		return id;
	}

	@Override
	public void registerUse()
	{
		used = true;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

	@Override
	public boolean isStaticId()
	{
		return true;
	}
}
