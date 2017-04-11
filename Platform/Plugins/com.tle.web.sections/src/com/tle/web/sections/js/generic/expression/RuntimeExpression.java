package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;

/**
 * A JSExpression where the contents can be decided at runtime.
 * 
 * @author jolz
 */
public class RuntimeExpression extends AbstractExpression
{
	@Override
	public String getExpression(RenderContext info)
	{
		return getRealExpression(info).getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(getRealExpression(info));
	}

	protected JSExpression getRealExpression(RenderContext info)
	{
		JSExpression expr = info.getAttribute(this);
		if( expr == null )
		{
			expr = createExpression(info);
			info.setAttribute(this, expr);
		}
		return expr;
	}

	public void setExpression(SectionInfo info, JSExpression expr)
	{
		if( expr == null )
		{
			throw new NullPointerException();
		}
		info.setAttribute(this, expr);
	}

	protected JSExpression createExpression(RenderContext info)
	{
		throw new SectionsRuntimeException("Expression not set and createExpression() not overridden"); //$NON-NLS-1$

	}
}
