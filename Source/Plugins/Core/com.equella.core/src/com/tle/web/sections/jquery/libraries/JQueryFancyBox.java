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
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryFancyBox implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude(
		DebugSettings.isDebuggingMode() ? "jquery.fancybox.js" : "jquery.fancybox.min.js",
		DebugSettings.isDebuggingMode() ? "fancybox/jquery.fancybox.css" : "fancybox/jquery.fancybox.min.css");

	public static final JSCallAndReference FANCYBOX = new ExternallyDefinedFunction("fancybox", -1, PRERENDER);

	public static final JSCallAndReference FANCYBOX_STATIC = new ExternallyDefinedFunction(JQueryCore.JQUERY,
		"fancybox", -1, PRERENDER);

	public static final JSCallAndReference CLOSE = new ExternallyDefinedFunction(FANCYBOX_STATIC, "close", -1);
	public static final JSCallAndReference SHOW_ACTIVITY = new ExternallyDefinedFunction(FANCYBOX_STATIC,
		"showActivity", 0);
	public static final JSCallAndReference HIDE_ACTIVITY = new ExternallyDefinedFunction(FANCYBOX_STATIC,
		"hideActivity", 0);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.fancybox.name");
	}

	@Override
	public String getId()
	{
		return "fancybox";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
