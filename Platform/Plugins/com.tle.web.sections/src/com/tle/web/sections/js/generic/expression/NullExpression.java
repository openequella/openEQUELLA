package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ServerSideValue;

/**
 * @author aholland
 */
@NonNullByDefault
public class NullExpression extends AbstractExpression implements ServerSideValue
{
	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return "null"; //$NON-NLS-1$
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

	@Override
	public String getJavaString()
	{
		return "null"; //$NON-NLS-1$
	}
}
