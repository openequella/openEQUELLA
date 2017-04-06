package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryUIAutocomplete implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	/**
	 * Includes ui.autocomplete
	 */
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.ui.autocomplete.js",
		JQueryUIWidget.PRERENDER, JQueryUIPosition.PRERENDER, JQueryUIMenu.PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.ui.autocomplete.name");
	}

	@Override
	public String getId()
	{
		return "uiautocomplete";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
