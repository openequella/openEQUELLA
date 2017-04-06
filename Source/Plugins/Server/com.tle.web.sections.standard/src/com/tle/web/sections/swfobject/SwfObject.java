package com.tle.web.sections.swfobject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class SwfObject implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	private static final String SWF_FILE = "js/swfobject.js"; //$NON-NLS-1$
	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(SwfObject.class);

	public static final PreRenderable PRERENDER = new IncludeFile(urlHelper.url(SWF_FILE));

	@Override
	public String getId()
	{
		return "swfobject";
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.standard.swfobject.name");
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
