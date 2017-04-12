package com.tle.web.sections.jquery.libraries;

import java.util.Map;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.DebugSettings;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.TagProcessor;

@SuppressWarnings("nls")
public class JQueryTextFieldHint implements TagProcessor, JavascriptModule
{
	private static final long serialVersionUID = 1L;

	public static final PreRenderable INCLUDE = new IncludeFile(ResourcesService.getResourceHelper(
		JQueryTextFieldHint.class).url("jquerylib/jquery.hint.js"));

	public static final JSCallable HINT_FUNC = new ExternallyDefinedFunction("hint", INCLUDE);

	private final Label hint;
	private ElementId element;

	/**
	 * Do not use this constructor
	 */
	public JQueryTextFieldHint()
	{
		hint = null;
		element = null;
	}

	public JQueryTextFieldHint(Label hint, ElementId element)
	{
		this.element = element;
		this.hint = hint;
	}

	@Override
	public void processAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		attrs.put("title", hint.getText());
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		if( element == null )
		{
			element = info.getBody();
		}
		if( !DebugSettings.isAutoTestMode() )
		{
			info.addReadyStatements(new JQueryStatement(element, new FunctionCallExpression(HINT_FUNC)));
		}
	}

	@Override
	public String getDisplayName()
	{
		return CurrentLocale.get("com.tle.web.sections.jquery.modules.textfieldhint.name");
	}

	@Override
	public String getId()
	{
		return "textfieldhint";
	}

	@Override
	public Object getPreRenderer()
	{
		return this;
	}
}
