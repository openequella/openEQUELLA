package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.BooleanExpression;

/**
 * @author aholland
 */
public class IfStatement implements JSStatements
{
	protected final BooleanExpression condition;
	protected final JSStatements body;

	public IfStatement(BooleanExpression condition, JSStatements body)
	{
		this.condition = condition;
		this.body = body;
	}

	@SuppressWarnings("nls")
	@Override
	public String getStatements(RenderContext info)
	{
		StringBuilder text = new StringBuilder("if (");
		text.append(condition.getExpression(info)).append(")").append(Js.NEWLINE).append("{").append(Js.NEWLINE)
			.append(body.getStatements(info)).append("}").append(Js.NEWLINE);

		return text.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, body, condition);
	}
}
