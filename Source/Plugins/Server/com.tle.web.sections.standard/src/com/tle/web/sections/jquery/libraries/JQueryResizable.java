package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

public class JQueryResizable implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.ui.resizable.js", //$NON-NLS-1$
		JQueryMouse.PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.resizable.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "resizable"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
