package com.tle.web.sections.js.generic.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ArrayExpression extends AbstractExpression
{
	private final Collection<JSExpression> exprs;

	public ArrayExpression()
	{
		exprs = new ArrayList<JSExpression>();
	}

	public ArrayExpression(JSExpression[] elems)
	{
		exprs = Arrays.asList(elems);
	}

	public ArrayExpression(Object... elems)
	{
		exprs = Arrays.asList(JSUtils.convertExpressions(elems));
	}

	public ArrayExpression(Collection<? extends JSExpression> exprs)
	{
		this();
		this.exprs.addAll(exprs);
	}

	public void add(Object obj)
	{
		exprs.add(JSUtils.convertExpression(obj));
	}

	public void addAll(Object... objs)
	{
		Collections.addAll(exprs, JSUtils.convertExpressions(objs));
	}

	public void addAll(JSExpression... exprs)
	{
		Collections.addAll(this.exprs, exprs);
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		StringBuilder sbuf = new StringBuilder("["); //$NON-NLS-1$
		boolean first = true;
		for( JSExpression expr : exprs )
		{
			if( !first )
			{
				sbuf.append(", "); //$NON-NLS-1$
			}
			sbuf.append(expr.getExpression(info));
			first = false;
		}
		sbuf.append("]"); //$NON-NLS-1$
		return sbuf.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(exprs);
	}

}
