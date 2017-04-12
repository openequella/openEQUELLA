package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.PreRenderable;

public class JQueryTimer implements JavascriptModule
{
	private static final long serialVersionUID = 1L;
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.timer.js"); //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static final ExternallyDefinedFunction TIMER_FUNC = new ExternallyDefinedFunction(
		PropertyExpression.create(JQueryCore.JQUERY, "timer"), -1, PRERENDER);

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.timer.name"); //$NON-NLS-1$
	}

	public static JSStatements createTimer(int millis, JSStatements body, ScriptVariable... vars)
	{
		return new FunctionCallStatement(TIMER_FUNC, millis, new AnonymousFunction(body, vars));
	}

	@Override
	public String getId()
	{
		return "timer"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
