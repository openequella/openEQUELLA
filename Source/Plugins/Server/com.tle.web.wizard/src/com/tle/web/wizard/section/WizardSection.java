package com.tle.web.wizard.section;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

public abstract class WizardSection<M> extends AbstractPrototypeSection<M> implements HtmlRenderer
{
	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;

	protected WizardSectionInfo getWizardInfo(SectionInfo info)
	{
		return info.getAttributeForClass(WizardSectionInfo.class);
	}
}
