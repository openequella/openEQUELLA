package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryUIAccordion implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	/**
	 * Includes jquery.ui.accordion.js
	 */
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.ui.accordion.js",
		JQueryUICore.PRERENDER, JQueryUIWidget.PRERENDER, JQueryUIEffects.PRERENDER_ALL);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.ui.accordion.name");
	}

	@Override
	public String getId()
	{
		return "uiaccordion";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
