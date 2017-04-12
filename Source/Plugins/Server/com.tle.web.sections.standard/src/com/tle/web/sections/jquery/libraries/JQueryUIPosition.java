package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryUIPosition implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	/**
	 * Includes ui.position
	 */
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.ui.position.js",
		JQueryUICore.PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.ui.position.name");
	}

	@Override
	public String getId()
	{
		return "uiposition";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
