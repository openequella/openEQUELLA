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

package com.tle.web.favourites.portal;

import java.util.Map;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;

@SuppressWarnings("nls")
@Bind
public class FavouritesPortletEditor
	extends
		AbstractPortletEditorSection<AbstractPortletEditorSection.AbstractPortletEditorModel>
{
	private static final String TYPE = "favourites";

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		// Nothing
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context,
		AbstractPortletEditorSection.AbstractPortletEditorModel model, PortletEditingBean portlet)
	{
		return new SimpleSectionResult("");
	}

	@Override
	public Class<AbstractPortletEditorSection.AbstractPortletEditorModel> getModelClass()
	{
		return AbstractPortletEditorSection.AbstractPortletEditorModel.class;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerSections(this, parentId);
	}

	@Override
	public SectionRenderable render(RenderContext info)
	{
		return renderSection(info, this);
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		// Nothing
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		// Nothing
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return viewFactory.createResult("portal/favportleteditorhelp.ftl", this);
	}
}
