package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQuerySlider implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQuerySlider.class);

	public static final PreRenderable PRERENDER = new IncludeFile(urlHelper.url("jquerylib/jquery.ui.slider.js"),
		JQueryMouse.PRERENDER);

	@Override
	public String getId()
	{
		return "slider";
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.slider.name");
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}

}
