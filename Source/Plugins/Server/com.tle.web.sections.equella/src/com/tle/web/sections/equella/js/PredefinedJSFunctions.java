package com.tle.web.sections.equella.js;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public final class PredefinedJSFunctions
{
	private static final IncludeFile JS = new IncludeFile(ResourcesService.getResourceHelper(
		PredefinedJSFunctions.class).url("scripts/predefinedfunctions.js"));

	public static final ExternallyDefinedFunction ENSURE_INPUT = new ExternallyDefinedFunction("ensureInput", JS);

	public static final ExternallyDefinedFunction ENSURE_SELECTED = new ExternallyDefinedFunction("ensureSelected", JS);

	private PredefinedJSFunctions()
	{
		throw new Error();
	}
}
