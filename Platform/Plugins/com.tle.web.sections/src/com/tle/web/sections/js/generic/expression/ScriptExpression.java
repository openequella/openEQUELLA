package com.tle.web.sections.js.generic.expression;

import java.util.Arrays;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.PreRenderable;

@NonNullByDefault
public class ScriptExpression extends AbstractExpression
{
	protected String expr;
	private List<PreRenderable> preRenderers;

	public ScriptExpression(String expr)
	{
		this.expr = expr;
	}

	public void setPreRenderers(PreRenderable... preRenderers)
	{
		this.preRenderers = Arrays.asList(preRenderers);
	}

	public ScriptExpression()
	{
		// dynamic it
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return expr;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(preRenderers);
	}
}
