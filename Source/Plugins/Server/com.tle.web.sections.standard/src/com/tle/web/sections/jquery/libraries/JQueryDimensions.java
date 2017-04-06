package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class JQueryDimensions implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.dimensions.js");

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.dimensions.name");
	}

	@Override
	public String getId()
	{
		return "dimensions";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
