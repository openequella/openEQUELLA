package com.tle.web.wizard;

import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.js.generic.function.IncludeFile;

public final class WizardJSLibrary
{
	static
	{
		PluginResourceHandler.init(WizardJSLibrary.class);
	}

	@PlugURL("scripts/wizardctrl.js")
	private static String URL_LIBRARY;

	public static final IncludeFile INCLUDE = new IncludeFile(URL_LIBRARY);

	private WizardJSLibrary()
	{
		throw new Error();
	}
}
