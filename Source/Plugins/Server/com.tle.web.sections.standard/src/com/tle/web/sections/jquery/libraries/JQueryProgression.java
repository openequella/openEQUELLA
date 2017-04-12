package com.tle.web.sections.jquery.libraries;

import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public final class JQueryProgression
{
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.progression.js");

	private static final ExternallyDefinedFunction FUNC = new ExternallyDefinedFunction("progression", PRERENDER);

	public static JSStatements createProgression(JQuerySelector selector)
	{
		return new JQueryStatement(selector, new FunctionCallExpression(FUNC));
	}

	public static JSStatements createProgression()
	{
		return createProgression(new JQuerySelector(Type.CLASS, "progressbar"));
	}

	private JQueryProgression()
	{
		throw new Error();
	}
}