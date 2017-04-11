package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.JSUtils;

/**
 * An immutable programattically defined Javascript function.
 * 
 * @author jolz
 */
public class SimpleFunction implements JSCallAndReference
{
	private final FunctionDefinition definition;
	protected int numParams;
	protected boolean staticName;

	public SimpleFunction(FunctionDefinition definition)
	{
		this.definition = definition;
	}

	public SimpleFunction(String name, ElementId id, JSStatements script, JSExpression... paramdefs)
	{
		this(new SimpleFunctionDefinition(name, id, script, paramdefs));
		staticName = id == null;
		this.numParams = paramdefs != null ? paramdefs.length : 0;
	}

	@Override
	public boolean isStatic()
	{
		return staticName;
	}

	public SimpleFunction(String name, JSStatements script, JSExpression... paramdefs)
	{
		this(name, null, script, paramdefs);
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return definition.getFunctionName(info);
	}

	@Override
	public String getExpressionForCall(RenderContext info, JSExpression... params)
	{
		return JSUtils.createFunctionCall(info, definition.getFunctionName(info), params);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addStatements(new FunctionDefinitionStatement(definition));
	}

	@Override
	public int getNumberOfParams(RenderContext context)
	{
		return numParams;
	}
}
