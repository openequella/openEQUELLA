package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.render.PreRenderable;

/**
 * @author aholland
 */
public class JQueryIDTabs implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.idTabs.min.js", //$NON-NLS-1$
		JQueryTabs.PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.idtabs.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "idtabs"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
