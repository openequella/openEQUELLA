package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryBiggerLink implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.biggerlink.js");

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.biggerlink.name");
	}

	@Override
	public String getId()
	{
		return "biggerlink";
	}

	@Override
	public PreRenderable getPreRenderer()
	{
		return PRERENDER;
	}
}