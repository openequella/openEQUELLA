package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;

public class SelectNotEmpty extends CombinedExpression
{
	private boolean skipFirstIndex = false;

	public SelectNotEmpty(ElementId id)
	{
		super(new ElementByIdExpression(id), new PropertyExpression("selectedIndex")); //$NON-NLS-1$
	}

	public SelectNotEmpty(ElementId id, boolean skipFirstIndex)
	{
		super(new ElementByIdExpression(id), new PropertyExpression("selectedIndex")); //$NON-NLS-1$
		this.skipFirstIndex = skipFirstIndex;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		if( skipFirstIndex )
		{
			return super.getExpression(info) + " >= 1"; //$NON-NLS-1$
		}
		else
		{
			return super.getExpression(info) + " >= 0"; //$NON-NLS-1$
		}
	}
}
