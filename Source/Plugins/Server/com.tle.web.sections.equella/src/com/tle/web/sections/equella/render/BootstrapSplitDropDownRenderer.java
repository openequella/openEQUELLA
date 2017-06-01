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
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;

/**
 * Render as button + dropdown as opposed to just a dropdown
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
public class BootstrapSplitDropDownRenderer extends BootstrapDropDownRenderer
{
	public static final String SPLIT_RENDER_CONSTANT = "bootstrapsplitdropdown";
	public static final String SPLIT_NAVBAR_RENDER_CONSTANT = "bootstrapsplitnavbardropdown";

	private final ButtonType buttonType;

	public BootstrapSplitDropDownRenderer(HtmlListState state)
	{
		this(state, null);
	}

	public BootstrapSplitDropDownRenderer(HtmlListState state, ButtonType buttonType)
	{
		super(state, false);
		this.buttonType = buttonType;
	}

	@Override
	protected void verifyState(HtmlListState state)
	{
		// Don't do anything. In other words, Abide
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.writeTag("div", "class", "btn-group");

		super.writeMiddle(writer);

		writer.endTag("div");
	}

	@Override
	protected void writeTrigger(SectionWriter writer) throws IOException
	{
		final Option<?> selected = getSelectedOption();
		// TODO: Uuugghhh! Would be nice to extract the actual Label from the
		// option, but we can't guarantee it's a LabelOption
		triggerLink.setLabel(new TextLabel(selected.getName(), false));
		triggerLink.setClickHandler(new OverrideHandler(clickFunc, selected.getValue()));
		triggerLink.addClass("btn");

		ButtonRenderer mainButton = new ButtonRenderer(triggerLink);
		if( buttonType != null )
		{
			mainButton = buttonType.apply(mainButton);
		}
		writer.render(mainButton);

		// toggle
		final HtmlComponentState toggleState = new HtmlComponentState();
		toggleState.addTagProcessor(Bootstrap.TOGGLE_ATTR);
		ButtonRenderer toggleButton = new ButtonRenderer(toggleState);
		if( buttonType != null )
		{
			toggleButton = buttonType.apply(toggleButton);
		}
		toggleButton.setNestedRenderable(caret());
		toggleButton.addClass("dropdown-toggle");
		writer.render(toggleButton);
	}



	private SectionRenderable caret()
	{
		final TagState state = new TagState();
		state.addClass("caret");
		return new TagRenderer("b", state);
	}
}
