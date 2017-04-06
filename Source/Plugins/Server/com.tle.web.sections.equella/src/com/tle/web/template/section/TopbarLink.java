package com.tle.web.template.section;

import com.tle.web.sections.standard.renderers.LinkRenderer;

public interface TopbarLink
{
	LinkRenderer getLink();

	void clearCachedCount();
}
