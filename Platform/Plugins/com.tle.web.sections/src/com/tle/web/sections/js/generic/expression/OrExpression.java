package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

/**
 * @author dustin
 */
@NonNullByDefault
public class OrExpression extends BooleanExpression
{
	protected final JSExpression first;
	protected final JSExpression second;

	public OrExpression(JSExpression first, JSExpression second)
	{
		this.first = first;
		this.second = second;
	}

	public OrExpression(String first, JSExpression second)
	{
		this(new ScriptExpression(first), second);
	}

	public OrExpression(String first, String second)
	{
		this(new ScriptExpression(first), new ScriptExpression(second));
	}

	@Override
	public String getExpression(RenderContext info)
	{
		StringBuilder text = new StringBuilder(first.getExpression(info));
		text.append(" || "); //$NON-NLS-1$
		text.append(second.getExpression(info));
		return text.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(first, second);
	}
}
