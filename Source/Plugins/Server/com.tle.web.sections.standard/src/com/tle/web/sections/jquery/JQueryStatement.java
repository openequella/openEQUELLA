package com.tle.web.sections.jquery;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;

public class JQueryStatement implements JSStatements
{
	private final JSExpression combined;

	public JQueryStatement(JQuerySelector selector, JSExpression property)
	{
		combined = PropertyExpression.create(selector, property);
	}

	public JQueryStatement(Type type, String id, String property)
	{
		this(new JQuerySelector(type, id), new ScriptExpression(property));
	}

	public JQueryStatement(Type type, String id, JSExpression property)
	{
		this(new JQuerySelector(type, id), property);
	}

	public JQueryStatement(ElementId id, String property)
	{
		this(new JQuerySelector(id), new ScriptExpression(property));
	}

	public JQueryStatement(ElementId id, JSExpression property)
	{
		this(new JQuerySelector(id), property);
	}

	@Override
	public String getStatements(RenderContext info)
	{
		return combined.getExpression(info) + ';';
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(combined);
	}
}
