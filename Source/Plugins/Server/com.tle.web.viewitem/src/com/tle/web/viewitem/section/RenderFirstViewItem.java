package com.tle.web.viewitem.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.render.RenderFirst;

public class RenderFirstViewItem extends RenderFirst implements ViewableChildInterface
{
	@Override
	public boolean canView(SectionInfo info)
	{
		return SectionUtils.canViewChildren(info, this);
	}
}
