package com.tle.web.sections.standard;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public interface RendererCallback
{
	void rendererSelected(RenderContext info, SectionRenderable renderer);
}
