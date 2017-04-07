package com.tle.web.sections.js.generic.statement;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ScriptVariable;

/**
 * @author aholland
 */
public class DeclarationStatement implements JSStatements
{
	protected final ScriptVariable var;
	protected final JSExpression defaultValue;

	public DeclarationStatement(ScriptVariable var, Object defaultValue)
	{
		this.var = var;
		this.defaultValue = JSUtils.convertExpression(defaultValue);
	}

	public DeclarationStatement(ScriptVariable var)
	{
		this(var, null);
	}

	@SuppressWarnings("nls")
	@Override
	public String getStatements(RenderContext info)
	{
		StringBuilder text = new StringBuilder("var ");
		text.append(var.getExpression(info));
		if( defaultValue != null )
		{
			text.append(" = ");
			text.append(defaultValue.getExpression(info));
		}
		text.append(";");
		text.append(Js.NEWLINE);
		return text.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, defaultValue);
	}
}
