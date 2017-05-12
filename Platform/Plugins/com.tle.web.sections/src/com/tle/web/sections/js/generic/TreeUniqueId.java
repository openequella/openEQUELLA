package com.tle.web.sections.js.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.js.ElementId;

@NonNullByDefault
public class TreeUniqueId implements ElementId
{
	private String id;
	private boolean used;

	public TreeUniqueId(SectionTree tree)
	{
		id = "t" + SectionUtils.getTreeUniqueId(tree); //$NON-NLS-1$
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
	public boolean isStaticId()
	{
		return true;
	}

	@Override
	public boolean isElementUsed()
	{
		return used;
	}

}
