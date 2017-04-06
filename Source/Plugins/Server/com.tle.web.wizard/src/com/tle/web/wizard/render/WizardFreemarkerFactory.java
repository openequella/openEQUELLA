package com.tle.web.wizard.render;

import javax.inject.Inject;

import com.google.inject.name.Named;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class WizardFreemarkerFactory extends WizardExtendedFactory
{
	private String defaultTitleTemplate = "title/standardTitle.ftl"; //$NON-NLS-1$
	private String defaultTailTemplate = "title/standardTail.ftl"; //$NON-NLS-1$
	@Inject
	@Named("TitleFactory")
	private WizardExtendedFactory titleFactory;

	@Override
	public SectionRenderable createResult(String template, SectionId sectionId)
	{
		return createWizardResult(template, defaultTitleTemplate, defaultTailTemplate, sectionId);
	}

	public DefaultWizardResult createWizardResult(String template, String title, String tail, SectionId sectionId)
	{
		final SectionRenderable normalResult = createNormalResult(template, sectionId);
		return createWizardResult(normalResult, title, tail, sectionId);
	}

	public DefaultWizardResult createWizardResult(SectionRenderable renderable, SectionId sectionId)
	{
		return createWizardResult(renderable, defaultTitleTemplate, defaultTailTemplate, sectionId);
	}

	public SectionRenderable createNormalResult(String template, SectionId sectionId)
	{
		return super.createResult(template, sectionId);
	}

	public DefaultWizardResult createWizardResult(SectionRenderable normalResult, String title, String tail,
		SectionId sectionId)
	{
		final SectionRenderable titleResult = titleFactory.createResult(title, sectionId);
		final SectionRenderable tailResult = titleFactory.createResult(tail, sectionId);

		return new DefaultWizardResult(titleResult, normalResult, tailResult);
	}

	public String getDefaultTitleTemplate()
	{
		return defaultTitleTemplate;
	}

	public void setDefaultTitleTemplate(String defaultTitleTemplate)
	{
		this.defaultTitleTemplate = defaultTitleTemplate;
	}

	public String getDefaultTailTemplate()
	{
		return defaultTailTemplate;
	}

	public void setDefaultTailTemplate(String defaultTailTemplate)
	{
		this.defaultTailTemplate = defaultTailTemplate;
	}

	public void setTitleFactory(WizardFreemarkerFactory titleFactory)
	{
		this.titleFactory = titleFactory;
	}
}
