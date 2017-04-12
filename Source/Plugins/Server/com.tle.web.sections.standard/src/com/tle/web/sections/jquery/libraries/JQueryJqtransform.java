package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class JQueryJqtransform implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude("jquery.jqtransform.js",
		"jquery.jqtransform.css");

	private static final JSCallable SETUP_JQTRANSFORM = new ExternallyDefinedFunction("jqTransform", PRERENDER);

	public static JSStatements setupJqtransform(JQuerySelector selector, ObjectExpression params)
	{
		return new JQueryStatement(selector, Js.call(SETUP_JQTRANSFORM, params));
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.jqtransform.name");
	}

	@Override
	public String getId()
	{
		return "jqtransform";
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
