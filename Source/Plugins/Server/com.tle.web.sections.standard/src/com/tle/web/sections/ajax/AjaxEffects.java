package com.tle.web.sections.ajax;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.ajax.handler.AjaxFunction;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public final class AjaxEffects
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(AjaxFunction.class);

	public static final IncludeFile EFFECTS_LIB = new IncludeFile(resources.url("js/ajaxeffects.js"),
		AjaxGenerator.AJAX_LIBRARY);
	private static final JSCallAndReference EFFECTS_CLASS = new ExternallyDefinedFunction("AjaxFX", EFFECTS_LIB);

	public static final ExternallyDefinedFunction FUNCTION_UPDATE_WITH_LOADING = new ExternallyDefinedFunction(
		EFFECTS_CLASS, "updateDomWithLoading", -1, EFFECTS_LIB);
	public static final ExternallyDefinedFunction FUNCTION_UPDATE_DOM_SILENT = new ExternallyDefinedFunction(
		EFFECTS_CLASS, "updateDomSilent", -1, EFFECTS_LIB);
	public static final ExternallyDefinedFunction FUNCTION_UPDATE_DOM_FADEIN = new ExternallyDefinedFunction(
		EFFECTS_CLASS, "updateDomFadeIn", -1, EFFECTS_LIB);
	public static final ExternallyDefinedFunction FUNCTION_FADE_DOM = new ExternallyDefinedFunction(EFFECTS_CLASS,
		"fadeDom", -1, EFFECTS_LIB);
	public static final ExternallyDefinedFunction FUNCTION_FADE_DOM_RESULTS = new ExternallyDefinedFunction(
		EFFECTS_CLASS, "fadeDomResults", -1, EFFECTS_LIB);
	public static final ExternallyDefinedFunction FUNCTION_UPDATE_WITH_ACTIVITY = new ExternallyDefinedFunction(
		EFFECTS_CLASS, "updateDomWithActivity", -1, EFFECTS_LIB);

	private AjaxEffects()
	{
		throw new Error();
	}
}
