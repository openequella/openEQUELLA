package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.PreRenderable;

@NonNullByDefault
public interface JSFunction extends PreRenderable
{
	int getNumberOfParams(RenderContext context);
}
