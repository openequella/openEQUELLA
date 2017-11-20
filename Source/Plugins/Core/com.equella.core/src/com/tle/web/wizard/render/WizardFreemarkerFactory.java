/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
