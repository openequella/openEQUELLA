package com.tle.web.sections.standard.js.modules;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class StandardModule implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final ExternallyDefinedFunction SET_TIMEOUT = new ExternallyDefinedFunction("setTimeout");

	public static final ExternallyDefinedFunction POPUP_PERCENT = new ExternallyDefinedFunction("popup_percent_xy");

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.standard.js.modules.standard.name");
	}

	@Override
	public String getId()
	{
		return "standard";
	}

	@Override
	public PreRenderable getPreRenderer()
	{
		return null;
	}
}
