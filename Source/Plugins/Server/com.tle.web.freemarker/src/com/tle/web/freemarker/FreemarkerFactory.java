package com.tle.web.freemarker;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;

@NonNullByDefault
public interface FreemarkerFactory
{
	SectionRenderable createResult(String template, SectionId sectionId);

	SectionRenderable createResult(String template, SectionId sectionId, PreRenderable preRenderer);

	SectionRenderable createResultWithModel(String template, Object model);

	SectionRenderable createResultWithModel(String template, Object... nameValues);

	NamedSectionResult createNamedResult(String name, String template, SectionId sectionId);

	TemplateResult createTemplateResult(String template, SectionId sectionId);

	TemplateResult createTemplateResultWithModel(String template, Object model);
}
