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

package com.tle.web.sections.equella.render;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.CssInclude.Priority;
import com.tle.web.sections.render.ExtraAttributes;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.TagProcessor;

@SuppressWarnings("nls")
public class Bootstrap implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	private static final String BOOTSTRAP_JS = "bootstrap/js/bootstrap.js";
	private static final String BOOTSTRAP_CSS = "bootstrap/css/bootstrap.css";
	private static final PluginResourceHelper URL_HELPER = ResourcesService.getResourceHelper(Bootstrap.class);

	public static final CssInclude CSS = CssInclude.include(URL_HELPER.url(BOOTSTRAP_CSS)).priority(Priority.LOWEST)
		.make();
	public static final PreRenderable PRERENDER = new IncludeFile(URL_HELPER.url(BOOTSTRAP_JS), CSS,
		JQueryCore.PRERENDER);
	public static final TagProcessor TOGGLE_ATTR = new ExtraAttributes("data-toggle", "dropdown");

	@Override
	public String getId()
	{
		return "bootstrap";
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.bootstrap.name");
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}