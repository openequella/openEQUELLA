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

package com.tle.web.portal.standard.editor;

import java.util.Map;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class SearchPortletEditorSection
	extends
		AbstractPortletEditorSection<AbstractPortletEditorSection.AbstractPortletEditorModel>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public String getDefaultPropertyName()
	{
		return "sch";
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet("search");
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		// Nothing by default
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing by default
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context,
		AbstractPortletEditorSection.AbstractPortletEditorModel model, PortletEditingBean portlet) throws Exception
	{
		return new SimpleSectionResult("");
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing by default
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing by default
	}

	@Override
	public Class<AbstractPortletEditorSection.AbstractPortletEditorModel> getModelClass()
	{
		return AbstractPortletEditorSection.AbstractPortletEditorModel.class;
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return viewFactory.createResult("help/searchportleteditorhelp.ftl", this);
	}
}
