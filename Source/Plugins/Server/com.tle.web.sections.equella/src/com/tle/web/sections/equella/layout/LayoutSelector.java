package com.tle.web.sections.equella.layout;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.template.Decorations;

public interface LayoutSelector
{
	TemplateResult getLayout(Decorations decorations, RenderContext info, TemplateResult templateResult)
		throws Exception;

	void preProcess(Decorations decorations);
}
