package com.tle.web.sections.equella.js;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.js.modules.JSONModule;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class StandardExpressions
{
	private static final PluginResourceHelper URL_HELPER = ResourcesService
		.getResourceHelper(StandardExpressions.class);
	// Other classes will need this, such as the ZebraTableRenderer
	public static final IncludeFile STANDARD_JS = new IncludeFile(URL_HELPER.url("scripts/standard.js"),
		JQueryCore.PRERENDER);
	private static final IncludeFile SUBMIT_JS = new IncludeFile(URL_HELPER.url("scripts/submit.js"),
		JSONModule.PRERENDERER, STANDARD_JS, BundleLabel.setupGlobalText(URL_HELPER.key("alreadysubmitting")));

	public static final String FORM_NAME = "eqpageForm";

	public static final JSExpression FORM_EXPRESSION = new FunctionCallExpression(new ExternallyDefinedFunction("_f",
		1, STANDARD_JS));

	public static final JSCallAndReference ELEMENT_FUNCTION = new ExternallyDefinedFunction("_e", 1, STANDARD_JS);

	public static final JSCallAndReference SUBMIT_FUNCTION = new ExternallyDefinedFunction("_sub", SUBMIT_JS);

	public static final JSCallAndReference SUBMIT_NOVAL_FUNCTION = new ExternallyDefinedFunction("_subnv", SUBMIT_JS);

	public static final JSCallAndReference SUBMIT_EVENT_FUNCTION = new ExternallyDefinedFunction("_subev", SUBMIT_JS);

	public static final JSCallAndReference SUBMIT_EVENT_NOVAL_FUNCTION = new ExternallyDefinedFunction("_subevnv",
		SUBMIT_JS);

	public static final JSCallAndReference TRIGGER_EVENT_FUNCTION = new ExternallyDefinedFunction("_trigger", 1,
		STANDARD_JS);

	public static final JSCallAndReference BIND_EVENT_FUNCTION = new ExternallyDefinedFunction("_bind", 3, STANDARD_JS);

	public static final JSCallAndReference BIND_W3C_FUNCTION = new ExternallyDefinedFunction("_bindW3C", 3, STANDARD_JS);

	private StandardExpressions()
	{
		throw new Error();
	}
}
