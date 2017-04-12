package com.tle.web.sections.jquery.libraries;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

public class JQueryFakeSelect implements JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable PRERENDER = new JQueryLibraryInclude(
		"jquery.fakeselect.js", "jquery.fakeselect.css"); //$NON-NLS-1$//$NON-NLS-2$

	public static final JSCallable FUNC_FAKE_SELECT = new ExternallyDefinedFunction("fakeselect", //$NON-NLS-1$
		PRERENDER);

	public static void convertSelect(RenderContext info, JQuerySelector selector)
	{
		JQueryCore.appendReady(info, new JQueryStatement(selector, new FunctionCallExpression(FUNC_FAKE_SELECT)));
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.fakeselect.name"); //$NON-NLS-1$
	}

	@Override
	public String getId()
	{
		return "fakeselect"; //$NON-NLS-1$
	}

	@Override
	public Object getPreRenderer()
	{
		return PRERENDER;
	}
}
