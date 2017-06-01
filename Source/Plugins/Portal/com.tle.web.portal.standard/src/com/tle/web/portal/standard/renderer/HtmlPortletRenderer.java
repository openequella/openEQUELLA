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

package com.tle.web.portal.standard.renderer;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class HtmlPortletRenderer extends PortletContentRenderer<HtmlPortletRenderer.Model>
{
	@Inject
	private HtmlEditorService htmlEditorService;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		getModel(context).setHtml(htmlEditorService.getHtmlRenderable(context, portlet.getConfig()));
		return viewFactory.createResult("htmlportlet.ftl", this);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pht";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private SectionRenderable html;

		public SectionRenderable getHtml()
		{
			return html;
		}

		public void setHtml(SectionRenderable html)
		{
			this.html = html;
		}
	}
}
