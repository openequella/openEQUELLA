package com.tle.web.wizard.render;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.render.SectionRenderable;

public interface WizardSectionResult extends SectionResult
{
	SectionRenderable getHtml();

	SectionRenderable getTitle();
}
