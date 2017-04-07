package com.tle.web.sections.events;

import com.tle.web.sections.SectionContext;

public interface RenderEventContext extends RenderContext, SectionContext
{
	RenderEvent getRenderEvent();
}
