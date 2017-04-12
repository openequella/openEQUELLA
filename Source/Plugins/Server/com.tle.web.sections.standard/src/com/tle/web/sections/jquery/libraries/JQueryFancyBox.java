package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryFancyBox implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.fancybox.js",
		"fancybox/jquery.fancybox.css");

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
