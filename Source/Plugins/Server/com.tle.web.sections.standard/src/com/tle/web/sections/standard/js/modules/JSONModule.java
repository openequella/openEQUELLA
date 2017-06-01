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

package com.tle.web.sections.standard.js.modules;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

public class JSONModule implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JSONModule.class);
	public static final PreRenderable PRERENDERER = new IncludeFile(urlHelper.url("js/json2.js")); //$NON-NLS-1$

	public static JSExpression getParseExpression(JSExpression text)
	{
		return new FunctionCallExpression(new ExternallyDefinedFunction("JSON.parse", PRERENDERER), text); //$NON-NLS-1$
	}

	public static JSExpression getStringifyExpression(JSExpression text)
	{
		return new FunctionCallExpression(new ExternallyDefinedFunction("JSON.stringify", PRERENDERER), text); //$NON-NLS-1$
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.standard.js.modules.json.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "json"; //$NON-NLS-1$
	}

	@Override
	public PreRenderable getPreRenderer()
	{
		return PRERENDERER;
	}
}
