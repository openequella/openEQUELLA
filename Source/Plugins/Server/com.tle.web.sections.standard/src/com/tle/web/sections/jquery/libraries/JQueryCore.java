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
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
@NonNullByDefault
public class JQueryCore implements PreRenderable, JavascriptModule
{
	private static final long serialVersionUID = 1L;

	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryCore.class);

	private static final String JQ_SHIM = urlHelper
		.url(DebugSettings.isDebuggingMode() ? "jquerycore/jquery-migrate.js" : "jquerycore/jquery-migrate.min.js");
	private static final String CORE_URL = urlHelper
		.url(DebugSettings.isDebuggingMode() ? "jquerycore/jquery.js" : "jquerycore/jquery.min.js");
	public static final IncludeFile PRERENDER = new IncludeFile(JQ_SHIM, new IncludeFile(CORE_URL));
	public static final ExternallyDefinedFunction JQUERY = new ExternallyDefinedFunction("$", PRERENDER);

	public static String getJQueryCoreUrl()
	{
		return CORE_URL;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(PRERENDER);
	}

	/**
	 * Use someElement.addReadyStatements(JSStatements) instead.
	 * 
	 * @param info
	 * @param statement
	 */
	@Deprecated
	public static void appendReady(RenderContext info, JSStatements statement)
	{
		info.getPreRenderContext().addReadyStatements(statement);
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.core.name");
	}

	@Override
	public String getId()
	{
		return "core";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
