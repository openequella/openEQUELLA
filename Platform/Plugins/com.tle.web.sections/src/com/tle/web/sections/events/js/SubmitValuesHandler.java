package com.tle.web.sections.events.js;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;

public class SubmitValuesHandler extends OverrideHandler
{
	private final JSExpression[] parameters;
	private final SubmitValuesFunction valuesFunction;

	public SubmitValuesHandler(ParameterizedEvent pevent, Object... params)
	{
		this(new SubmitValuesFunction(pevent), JSUtils.convertExpressions(params));
	}

	public SubmitValuesHandler(SubmitValuesFunction valuesFunction, JSExpression[] params)
	{
		this.parameters = params;
		this.valuesFunction = valuesFunction;
		addStatements(new FunctionCallStatement(valuesFunction, (Object[]) params));
	}

	public JSExpression[] getParameters()
	{
		return parameters;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.preRender(parameters);
	}

	public SubmitValuesHandler setValidate(boolean validate)
	{
		valuesFunction.setValidate(validate);
		return this;
	}

	@Override
	public JSBookmarkModifier getModifier()
	{
		return new EventModifier(valuesFunction.getFirstParam(), valuesFunction.getEvent(), (Object[]) parameters);
	}
}
