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
public class IfElseStatement extends IfStatement
{
	private final JSStatements elseBody;

	public IfElseStatement(BooleanExpression condition, JSStatements body, JSStatements elseBody)
	{
		super(condition, body);
		this.elseBody = elseBody;
	}

	@SuppressWarnings("nls")
	@Override
	public String getStatements(RenderContext info)
	{
		StringBuilder text = new StringBuilder(super.getStatements(info));
		text.append("else").append(Js.NEWLINE).append("{").append(Js.NEWLINE).append(elseBody.getStatements(info))
			.append(Js.NEWLINE).append("}").append(Js.NEWLINE);
		return text.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, body, condition);
	}
}
