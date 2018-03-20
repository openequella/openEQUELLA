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

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.DebugSettings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public class JQueryScrollTo implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryScrollTo.class);

	public static final IncludeFile INCLUDE = new IncludeFile(urlHelper.url("jquerylib/jquery.scrollTo.js"),
			JQueryCore.PRERENDER).hasMin();

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.scrollto.name");
	}

	@Override
	public String getId()
	{
		return "scrollto";
	}

	@Override
	public Object getPreRenderer()
	{
		return INCLUDE;
	}
}
