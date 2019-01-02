/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.ButtonRenderer;

/**
 * The simplest standard component.
 * <p>
 * To make a {@code Button} useful, it needs to have {@link JSHandler}s attached
 * to it. <br>
 * The default renderer is usually {@link ButtonRenderer}.
 * 
 * @author jmaginnis
 */
@SuppressWarnings("nls")
public class Button extends AbstractEventOnlyComponent<HtmlComponentState>
{
	public static final String ICON_CLASS_KEY = "icon_class";
	public static final String ICON_COLOUR_KEY = "icon_colour";
	private boolean cancel;

	public Button()
	{
		super(RendererConstants.BUTTON);
	}

	@Override
	public Class<HtmlComponentState> getModelClass()
	{
		return HtmlComponentState.class;
	}

	@Override
	protected HtmlComponentState setupState(SectionInfo info, HtmlComponentState state)
	{
		super.setupState(info, state);
		state.setCancel(cancel);
		return state;
	}

	public boolean isCancel()
	{
		return cancel;
	}

	public void setCancel(boolean cancel)
	{
		this.cancel = cancel;
	}
}
