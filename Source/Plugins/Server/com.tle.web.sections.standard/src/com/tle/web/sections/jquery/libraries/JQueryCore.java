package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

public class JQueryCore implements PreRenderable, JavascriptModule
{
	private static final long serialVersionUID = 1L;

	private static final String JQUERY_FILE = "jquerycore/jquery.js"; //$NON-NLS-1$
	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryCore.class);
	public static final JQueryCore PRERENDER = new JQueryCore();

	public static final ExternallyDefinedFunction JQUERY = new ExternallyDefinedFunction("$", PRERENDER); //$NON-NLS-1$

	public static String getJQueryCoreUrl()
	{
		return urlHelper.url(JQUERY_FILE);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addJs(JQueryCore.getJQueryCoreUrl());
	}

	/**
	 * Use someElement.addReadyStatements(JSStatements) instead.
	 * 
	 * @param info
	 * @param statement
	 */
	@Deprecated
	public static void appendReady(RenderContext info, JSStatements statement)
	{
		info.getPreRenderContext().addReadyStatements(statement);
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.core.name"); //$NON-NLS-1$;
	}

	@Override
	public String getId()
	{
		return "core"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
