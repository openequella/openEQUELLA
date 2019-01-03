/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.standard.renderers.toggle;

import java.io.IOException;
import java.util.Map;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQueryLibraryInclude;
import com.tle.web.sections.jquery.libraries.JQueryUIWidget;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.renderers.AbstractComponentRenderer;

@SuppressWarnings("nls")
public class JQueryUITogglerRenderer extends AbstractComponentRenderer
{
	private final HtmlBooleanState bstate;

	private String checkedText;

	private String uncheckedText;

	private static final PluginResourceHelper urlHelper = ResourcesService
		.getResourceHelper(JQueryUITogglerRenderer.class);

	private static final IncludeFile TOGGLE_JS = new IncludeFile(urlHelper.url("js/toggle.js"));
	private static JSCallable SETUP_FUNC = new ExternallyDefinedFunction("toggle", 1, TOGGLE_JS);

	public JQueryUITogglerRenderer(HtmlBooleanState state)
	{
		super(state);
		this.bstate = state;
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		attrs.put("id", state.getName());
		super.prepareFirstAttributes(writer, attrs);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		super.writeMiddle(writer);

		String name = state.getName();
		HtmlBooleanState yesBox = new HtmlBooleanState();

		yesBox.setName(name);
		yesBox.setId(name + "_1");
		yesBox.setValue("checked");

		HtmlBooleanState noBox = new HtmlBooleanState();
		noBox.setName(name);
		noBox.setId(name + "_2");

		if( bstate.isChecked() )
		{
			yesBox.setChecked(true);
		}
		else
		{
			noBox.setChecked(true);
		}

		writer.addReadyStatements(new FunctionCallStatement(SETUP_FUNC, name));

		writer.render(new RadioButtonRenderer(yesBox));
		writer.writeTag("label", "for", name + "_1");
		writer.writeText(getCheckedText());
		writer.endTag("label");

		writer.render(new RadioButtonRenderer(noBox));
		writer.writeTag("label", "for", name + "_2");
		writer.writeText(getUncheckedText());
		writer.endTag("label");
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.preRender(new JQueryLibraryInclude("jquery.ui.button.js", JQueryUIWidget.PRERENDER));
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !event.equals(JSHandler.EVENT_CHANGE) )
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	@Override
	protected String getTag()
	{
		return "div";
	}

	public void setCheckedText(String checkedText)
	{
		this.checkedText = checkedText;
	}

	public String getCheckedText()
	{
		if( Check.isEmpty(checkedText) )
		{
			return CurrentLocale.get("com.tle.web.sections.standard.renderers.toggle.yes");
		}
		else
		{
			return checkedText;
		}
	}

	public void setUncheckedText(String uncheckedText)
	{
		this.uncheckedText = uncheckedText;
	}

	public String getUncheckedText()
	{
		if( Check.isEmpty(uncheckedText) )
		{
			return CurrentLocale.get("com.tle.web.sections.standard.renderers.toggle.no");
		}
		else
		{
			return uncheckedText;
		}
	}
}
