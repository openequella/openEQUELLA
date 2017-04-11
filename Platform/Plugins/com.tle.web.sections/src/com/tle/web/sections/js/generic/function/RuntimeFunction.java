package com.tle.web.sections.js.generic.function;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

/**
 * A JSCallAndReference which can be created or decided on at runtime.
 * 
 * @author jolz
 */
@NonNullByDefault
public abstract class RuntimeFunction implements JSCallable
{
	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		return getRealFunction(info).getExpressionForCall(info, params);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(getRealFunction(info));
	}

	protected JSCallable getRealFunction(RenderContext info)
	{
		JSCallable callable = info.getAttribute(this);
		if( callable == null )
		{
			callable = createFunction(info);
			info.setAttribute(this, callable);
		}
		return callable;
	}

	public boolean hasBeenSet(SectionInfo info)
	{
		return info.getAttribute(this) != null;
	}

	public void setFunction(SectionInfo info, JSCallable callable)
	{
		info.setAttribute(this, callable);
	}

	protected JSCallable createFunction(RenderContext info)
	{
		throw new SectionsRuntimeException("Function not set and createFunction() not overridden"); //$NON-NLS-1$
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return getRealFunction(context).getNumberOfParams(context);
	}
}
