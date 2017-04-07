package com.tle.web.sections.js.generic.statement;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;

/**
 * @author aholland
 */
@NonNullByDefault
public class AssignStatement implements JSStatements
{
	protected final JSExpression var;
	protected final JSExpression value;

	public AssignStatement(JSExpression var, Object value)
	{
		JSExpression[] exprs = JSUtils.convertExpressions(var, value);
		this.var = exprs[0];
		this.value = exprs[1];
	}

	@SuppressWarnings("nls")
	@Override
	public String getStatements(RenderContext info)
	{
		return var.getExpression(info) + " = " + value.getExpression(info) + ";";
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(var, value);
	}
}
