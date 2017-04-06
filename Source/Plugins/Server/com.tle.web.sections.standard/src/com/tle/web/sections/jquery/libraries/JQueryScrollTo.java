package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;

public class JQueryScrollTo implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryScrollTo.class);

	@SuppressWarnings("nls")
	public static final IncludeFile INCLUDE = new IncludeFile(urlHelper.url("jquerylib/jquery.scrollTo.js"),
		JQueryCore.PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.scrollto.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "scrollto"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return INCLUDE;
	}
}
