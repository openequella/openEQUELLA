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

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.renderer.PortletContentRenderer;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
@Bind
@SuppressWarnings("nls")
public class ShowcasePortletRenderer extends PortletContentRenderer<Object>
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(ShowcasePortletRenderer.class);

	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionRenderable renderHtml(RenderEventContext context) throws Exception
	{
		context.getPreRenderContext().addCss(RESOURCES.url("css/showcase.css"));
		return view.createResult("showcaseportlet.ftl", context);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "psc";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
