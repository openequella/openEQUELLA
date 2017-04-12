package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQuerySortable implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	/**
	 * Includes ui.mouse
	 */
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.ui.sortable.js",
		JQueryMouse.PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.sortable.name");
	}

	@Override
	public String getId()
	{
		return "sortable";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
