package com.tle.web.sections.js.generic.statement;

import java.util.Arrays;
import java.util.List;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.PreRenderable;

/**
 * Used for plain old string pieces of Javascript
 */
public class ScriptStatement implements JSStatements
{
	// Snippets
	public static final ScriptStatement WINDOW_CLOSE = new ScriptStatement("window.close();"); //$NON-NLS-1$
	public static final ScriptStatement HISTORY_BACK = new ScriptStatement("history.back();"); //$NON-NLS-1$

	private String rawExpr;
	private JSExpression expr;
	private List<PreRenderable> preRenderers;

	public ScriptStatement(String rawExpr)
	{
		this.rawExpr = rawExpr;
		this.expr = null;
	}

	public ScriptStatement(JSExpression expr)
	{
		this.expr = expr;
		this.rawExpr = null;
	}

	public void setPreRenderers(PreRenderable... preRenderers)
	{
		this.preRenderers = Arrays.asList(preRenderers);
	}

	public ScriptStatement()
	{
		// dynamic it
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return (rawExpr == null ? expr.getExpression(info) + ';' : rawExpr);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, expr);
		SectionUtils.preRender(info, preRenderers);
	}
}
