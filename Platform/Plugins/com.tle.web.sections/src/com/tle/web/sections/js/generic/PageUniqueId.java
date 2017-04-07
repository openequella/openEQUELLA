package com.tle.web.sections.js.generic;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.ElementId;

public class PageUniqueId implements ElementId
{
	private boolean used;

	@Override
	@SuppressWarnings("nls")
	public String getElementId(SectionInfo info)
	{
		String id = info.getAttribute(this);
		if( id == null )
		{
			String prepend = info.getAttribute(PageUniqueId.class);
			prepend = prepend == null ? "i" : prepend + "_i";
			id = prepend + SectionUtils.getPageUniqueId(info);
			info.setAttribute(this, id);
		}
		return id;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

	@Override
	public boolean isStaticId()
	{
		return false;
	}

	@Override
	public void registerUse()
	{
		used = true;
	}
}
