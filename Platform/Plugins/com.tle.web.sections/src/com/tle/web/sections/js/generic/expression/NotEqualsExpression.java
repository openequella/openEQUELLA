package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

/**
 * @author aholland
 */
@NonNullByDefault
public class NotEqualsExpression extends BooleanExpression
{
	protected final JSExpression first;
	protected final JSExpression second;

	public NotEqualsExpression(JSExpression first, JSExpression second)
	{
		this.first = first;
		this.second = second;
	}

	public NotEqualsExpression(String first, JSExpression second)
	{
		this(new ScriptExpression(first), second);
	}

	public NotEqualsExpression(String first, String second)
	{
		this(new ScriptExpression(first), new ScriptExpression(second));
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		StringBuilder text = new StringBuilder(first.getExpression(info));
		text.append(" != "); //$NON-NLS-1$
		text.append(second.getExpression(info));
		return text.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(first, second);
	}
}
