package com.tle.web.sections.render;

import com.tle.web.sections.events.RenderContext;

public interface TemplateRenderable extends SectionRenderable
{
	boolean exists(RenderContext context);
}
