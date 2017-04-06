package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;

public class JQueryUICore implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	protected static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryUICore.class);

	@SuppressWarnings("nls")
	public static final PreRenderable PRERENDER = new IncludeFile(urlHelper.url("jquerylib/jquery.ui.core.js"),
		JQueryCore.PRERENDER, new CssInclude(urlHelper.url("css/themes/equella/jquery-ui.css")));

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.ui.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "ui"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
