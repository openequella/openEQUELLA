package com.tle.web.sections.standard.renderers;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

public class SelectOptionsCallback extends SimpleFunction
{
	private static PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(SelectOptionsCallback.class);

	public SelectOptionsCallback(String id, JSExpression element)
	{
		super("changeOptions" + id, new FunctionCallStatement("changeSelectOptions", element, //$NON-NLS-1$ //$NON-NLS-2$
			new CombinedExpression(AjaxGenerator.RESULTS_VAR, new PropertyExpression("result"))), //$NON-NLS-1$
			AjaxGenerator.RESULTS_VAR, AjaxGenerator.STATUS_VAR);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addJs(urlHelper.url("js/select.js")); //$NON-NLS-1$
		super.preRender(info);
	}
}
