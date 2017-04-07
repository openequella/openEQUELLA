package com.tle.web.sections.render;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderContext;

public interface TemplateResult extends SectionResult
{
	TemplateRenderable getNamedResult(RenderContext info, String name);
}
