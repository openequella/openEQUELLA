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

package com.tle.web.sections.jquery.libraries;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.DebugSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
@NonNullByDefault
public class JQueryUICore implements PreRenderable, JavascriptModule
{
	private static final long serialVersionUID = 1L;

	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryUICore.class);

	public static final PreRenderable PRERENDER = new IncludeFile(
		urlHelper
			.url(DebugSettings.isDebuggingMode() ? "jquerylib/jquery.ui.core.js" : "jquerylib/jquery.ui.core.min.js"),
		JQueryCore.PRERENDER, new CssInclude(urlHelper.url(DebugSettings.isDebuggingMode()
			? "css/themes/equella/jquery-ui.css" : "css/themes/equella/jquery-ui.min.css")));

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(PRERENDER);
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.ui.name");
	}

	@Override
	public String getId()
	{
		return "ui";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
