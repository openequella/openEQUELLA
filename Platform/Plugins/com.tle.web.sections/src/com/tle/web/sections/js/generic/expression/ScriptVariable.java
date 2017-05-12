package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;

/**
 * @author aholland
 */
public class ScriptVariable extends AbstractExpression
{
	protected final String name;
	private final ElementId elementId;

	public ScriptVariable(String name)
	{
		this(name, null);
	}

	public ScriptVariable(String name, ElementId id)
	{
		this.name = name;
		this.elementId = id;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		if( elementId == null )
		{
			return name;
		}
		return name + elementId.getElementId(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}
}
