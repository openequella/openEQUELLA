package com.tle.web.sections.equella.render;

import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryUIAutocomplete;
import com.tle.web.sections.jquery.libraries.JQueryUIPosition;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;

@SuppressWarnings("nls")
public class RichDropDownRenderer extends DropDownRenderer
{
	static
	{
		PluginResourceHandler.init(RichDropDownRenderer.class);
	}

	@PlugURL("scripts/component/richdropdown.js")
	private static String RICHDROPDOWN_URL;

	@PlugURL("css/component/richdropdown.css")
	private static String RICHDROPDOWN_STYLE;

	private static JSCallable SETUP_RICHDROPDOWN = new ExternallyDefinedFunction(
		"setupRichDropDown", new IncludeFile(RICHDROPDOWN_URL), JQueryUIAutocomplete.PRERENDER, //$NON-NLS-1$
		JQueryCore.PRERENDER, JQueryUIPosition.PRERENDER);

	private static final CssInclude CSS = CssInclude.include(RICHDROPDOWN_STYLE).hasRtl().make();

	public RichDropDownRenderer(HtmlListState state)
	{
		super(state);
		state.setDisabled(false);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(CSS);
		info.addReadyStatements(new JQueryStatement(new JQuerySelector(this), new FunctionCallExpression(
			SETUP_RICHDROPDOWN)));
	}

	@Override
	protected String getTag()
	{
		return "select";
	}
}
