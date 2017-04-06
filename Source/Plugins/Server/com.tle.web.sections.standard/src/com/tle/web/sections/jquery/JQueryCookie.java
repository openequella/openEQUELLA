package com.tle.web.sections.jquery;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryEquellaUtils;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryCookie implements JavascriptModule, PreRenderable
{
	private static final long serialVersionUID = 1L;

	public static final IncludeFile PRERENDER = new IncludeFile(ResourcesService.getResourceHelper(
		JQueryEquellaUtils.class).url("js/jquery.cookie.js"), JQueryCore.PRERENDER);

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(PRERENDER);
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.cookie.name");
	}

	@Override
	public String getId()
	{
		return "cookie";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}

