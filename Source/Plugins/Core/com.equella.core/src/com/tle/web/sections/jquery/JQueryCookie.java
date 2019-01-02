/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.jquery;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryEquellaUtils;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryCookie implements JavascriptModule, PreRenderable
{
	private static final long serialVersionUID = 1L;

	public static final IncludeFile PRERENDER = new IncludeFile(ResourcesService.getResourceHelper(
		JQueryEquellaUtils.class).url("js/jquery.cookie.js"), JQueryCore.PRERENDER);

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(PRERENDER);
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.cookie.name");
	}

	@Override
	public String getId()
	{
		return "cookie";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}

