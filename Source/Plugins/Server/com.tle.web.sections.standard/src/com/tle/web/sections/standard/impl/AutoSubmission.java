package com.tle.web.sections.standard.impl;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class AutoSubmission implements PreRenderable
{
	private static final PluginResourceHelper URL_HELPER = ResourcesService.getResourceHelper(AutoSubmission.class);

	protected static final String NAMESPACE = "AutoSubmit.";
	protected static final JSCallable SETUP = new ExternallyDefinedFunction(NAMESPACE + "setupAutoSubmit",
		new IncludeFile(URL_HELPER.url("js/autosubmit.js")), new CssInclude(URL_HELPER.url("css/autosubmit.css")));

	protected final ElementId control;
	protected final ElementId autoSubmitButton;

	public AutoSubmission(ElementId control, ElementId autoSubmitButton)
	{
		this.control = control;
		this.autoSubmitButton = autoSubmitButton;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		JQueryCore.appendReady(info, new FunctionCallStatement(SETUP, new ElementByIdExpression(control),
			new ElementByIdExpression(autoSubmitButton)));
	}
}
