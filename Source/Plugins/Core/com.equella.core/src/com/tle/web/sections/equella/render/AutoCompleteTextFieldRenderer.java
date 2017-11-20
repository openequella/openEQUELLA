/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.equella.render;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryUIAutocomplete;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlTextFieldState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;

@SuppressWarnings("nls")
public class AutoCompleteTextFieldRenderer extends AbstractComponentRenderer implements JSDisableable
{
	static
	{
		PluginResourceHandler.init(AutoCompleteTextFieldRenderer.class);
	}

	@PlugURL("scripts/component/autocomplete.js")
	private static String AUTOCOMPLETE_URL;
	@PlugURL("css/component/autocomplete.css")
	private static String AUTOCOMPLETE_STYLE;

	private static JSCallable SETUP_AUTOCOMPLETE = new ExternallyDefinedFunction(
		"autocompletesearch", new IncludeFile(AUTOCOMPLETE_URL), JQueryUIAutocomplete.PRERENDER, //$NON-NLS-1$
		JQueryCore.PRERENDER);

	private static final CssInclude CSS = CssInclude.include(AUTOCOMPLETE_STYLE).make();

	private final HtmlTextFieldState htmlTextFieldState;
	private final TextFieldRenderer tfrReal;
	private final TextFieldRenderer tfrPrompt;

	public AutoCompleteTextFieldRenderer(HtmlTextFieldState state)
	{
		super(state);
		addClass("autocomplete-container");

		htmlTextFieldState = state;

		final HtmlValueState value = new HtmlValueState();
		value.setId(state.getId());
		value.setName(state.getName());
		value.setValue(state.getValue());
		tfrReal = new TextFieldRenderer(value);
		tfrReal.addClass("real");

		final HtmlValueState prompt = new HtmlValueState();
		prompt.setDisabled(true);
		prompt.setElementId(new AppendedElementId(state, "_prompt"));
		tfrPrompt = new TextFieldRenderer(prompt);
		tfrPrompt.addClass("prompt");
	}

	@Override
	public ElementId getVisibleElementId()
	{
		AppendedElementId divId = new AppendedElementId(this, "_autocomplete");
		divId.registerUse();
		return divId;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);

		info.preRender(CSS);

		JSCallable callback = htmlTextFieldState.getAutoCompleteCallback();
		JSHandler handler = htmlTextFieldState.getHandler("autoselect");
		if( callback != null )
		{
			if( handler != null )
			{
				info.addReadyStatements(Js.call_s(SETUP_AUTOCOMPLETE, callback, getVisibleElementId(),
					new ObjectExpression("autoselect", new AnonymousFunction(handler))));
			}
			else
			{
				info.addReadyStatements(Js.call_s(SETUP_AUTOCOMPLETE, callback, getVisibleElementId(),
					new ObjectExpression()));
			}
		}
	}

	public void setAutoSubmitButton(final Object autoSubmitButton)
	{
		tfrReal.setAutoSubmitButton(autoSubmitButton);
	}

	public void setPlaceholderText(final String text)
	{
		tfrReal.setPlaceholderText(text);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.render(tfrReal);
		if( nestedRenderable != null )
		{
			writer.render(nestedRenderable);
		}
		writer.render(tfrPrompt);
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new DefaultDisableFunction(this);
	}

	@Override
	protected String getTag()
	{
		return "div";
	}
}
