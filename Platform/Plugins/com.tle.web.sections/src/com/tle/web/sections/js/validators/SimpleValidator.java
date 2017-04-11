package com.tle.web.sections.js.validators;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSValidator;
import com.tle.web.sections.js.generic.statement.ScriptStatement;

public class SimpleValidator implements JSValidator
{
	private JSStatements failureStatments;
	private final JSExpression validatorExpression;
	private boolean returnFalse = true;

	public SimpleValidator(JSExpression validatorExpression)
	{
		failureStatments = new ScriptStatement(""); //$NON-NLS-1$
		this.validatorExpression = validatorExpression;
	}

	@Override
	public JSValidator setFailureStatements(JSStatements statements)
	{
		this.failureStatments = statements;
		return this;
	}

	public String getValidatorExpressionText(RenderContext info)
	{
		return validatorExpression.getExpression(info);
	}

	@SuppressWarnings("nls")
	@Override
	public String getStatements(RenderContext info)
	{
		return "if (!(" + getValidatorExpressionText(info) + ")){" + failureStatments.getStatements(info)
			+ (returnFalse ? "return false;" : "") + "}";
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(failureStatments, validatorExpression);
	}

	public void setReturnFalse(boolean returnFalse)
	{
		this.returnFalse = returnFalse;
	}

}
