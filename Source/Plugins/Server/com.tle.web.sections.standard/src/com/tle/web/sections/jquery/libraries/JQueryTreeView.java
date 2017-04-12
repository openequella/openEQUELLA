package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

public class JQueryTreeView implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("nls")
	public static final PreRenderable PRERENDER = new JQueryLibraryInclude(new String[]{"jquery.treeview.js",
			"jquery.treeview.async.js",}, "jquery.treeview.css", true);

	public static final JSCallable FUNC_TREEVIEW = new ExternallyDefinedFunction("treeview", PRERENDER); //$NON-NLS-1$

	public static JQueryStatement treeView(ElementId tag, JSExpression params)
	{
		return new JQueryStatement(tag, new FunctionCallExpression(FUNC_TREEVIEW, params));
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.treeview.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "treeview"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
