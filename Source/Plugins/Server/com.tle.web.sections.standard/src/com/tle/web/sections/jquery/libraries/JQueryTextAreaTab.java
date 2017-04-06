package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

public class JQueryTextAreaTab implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.textarea.js"); //$NON-NLS-1$

	public static final JSCallable FUNC_TEXTAREATAB = new ExternallyDefinedFunction("tabby", PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.textarea.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "textarea"; //$NON-NLS-1$
	}

	@Override
	public PreRenderable getPreRenderer()
	{
		return PRERENDER;
	}
}