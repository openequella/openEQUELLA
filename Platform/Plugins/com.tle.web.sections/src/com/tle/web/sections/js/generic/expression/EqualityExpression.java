package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

/**
 * @author aholland
 */
public class EqualityExpression extends BooleanExpression
{
	protected final JSExpression first;
	protected final JSExpression second;

	public EqualityExpression(Object first, Object second)
	{
		this.first = JSUtils.convertExpression(first);
		this.second = JSUtils.convertExpression(second);
	}

	public EqualityExpression(String first, JSExpression second)
	{
		this(new ScriptExpression(first), second);
	}

	public EqualityExpression(String first, String second)
	{
		this(new ScriptExpression(first), new ScriptExpression(second));
	}

	@Override
	public String getExpression(RenderContext info)
	{
		StringBuilder text = new StringBuilder(first.getExpression(info));
		text.append(" == "); //$NON-NLS-1$
		text.append(second.getExpression(info));
		return text.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(first, second);
	}
}
