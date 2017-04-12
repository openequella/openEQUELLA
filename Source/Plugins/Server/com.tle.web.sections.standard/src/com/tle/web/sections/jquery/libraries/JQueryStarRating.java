package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class JQueryStarRating implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.stars.js", "jquery.stars.css",
		JQueryUIWidget.PRERENDER);

	public static final JSCallable FUNC_STARRATING = new ExternallyDefinedFunction("stars", PRERENDER);

	public static void starRating(RenderContext info, JSExpression selector, JSExpression params)
	{
		JQueryCore.appendReady(
			info,
			new ScriptStatement(PropertyExpression
				.create(selector, new FunctionCallExpression(FUNC_STARRATING, params))));
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.starrating.name");
	}

	@Override
	public String getId()
	{
		return "starrating";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
