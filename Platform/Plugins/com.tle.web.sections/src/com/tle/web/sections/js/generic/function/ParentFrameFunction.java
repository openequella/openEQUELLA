package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@SuppressWarnings("nls")
@NonNullByDefault
public class ParentFrameFunction extends AbstractCallable
{
	private final JSCallAndReference parentFunction;

	public ParentFrameFunction(JSCallAndReference parentFunction)
	{
		if( !parentFunction.isStatic() )
		{
			throw new SectionsRuntimeException("You can only use static functions with parent frame functions");
		}
		this.parentFunction = parentFunction;
	}

	@Override
	protected String getCallExpression(RenderContext info, JSExpression[] params)
	{
		return JSUtils.createFunctionCall(info, "self.parent." + parentFunction.getExpression(info), params);
	}

	@Override
	public int getNumberOfParams(@Nullable RenderContext context)
	{
		return parentFunction.getNumberOfParams(context);
	}

	@Override
	public void preRender(PreRenderContext writer)
	{
		// nothing.. it happens on the parent
	}
}
