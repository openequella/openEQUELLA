package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

/**
 * @author Aaron
 */
public class JQueryStylishSelect implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.stylish-select.js",
		"jquery.stylish-select.css");

	private static final JSCallable SETUP_STYLISH = new ExternallyDefinedFunction("sSelect", PRERENDER);

	public static JSStatements setupStylishSelect(JQuerySelector selector, ObjectExpression params)
	{
		return new JQueryStatement(selector, new FunctionCallExpression(SETUP_STYLISH, params));
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.stylishselect.name");
	}

	@Override
	public String getId()
	{
		return "stylish-select"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
