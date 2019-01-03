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

package com.tle.web.sections.standard.renderers;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.impl.AutoSubmission;
import com.tle.web.sections.standard.model.HtmlTextFieldState;
import com.tle.web.sections.standard.model.HtmlValueState;

public class AbstractTextFieldRenderer extends AbstractInputRenderer
{
	protected final HtmlValueState valueState;
	private boolean noLabel;
	protected final boolean editable;
	protected final boolean autocompleteDisabled;
	private AutoSubmission autoSubmitRenderer;

	public AbstractTextFieldRenderer(HtmlValueState state, String type)
	{
		super(state, type);
		this.autocompleteDisabled = false;
		this.editable = true;
		this.valueState = state;
	}

	public AbstractTextFieldRenderer(HtmlTextFieldState state, String type)
	{
		super(state, type);
		this.autocompleteDisabled = state.isDisabled();
		this.editable = state.isEditable();
		this.valueState = state;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		if( autoSubmitRenderer != null )
		{
			autoSubmitRenderer.preRender(info);
		}
	}

	public void setAutoSubmitButton(final Object autoSubmitButton)
	{
		if( autoSubmitButton != null
			&& !(autoSubmitButton instanceof String && ((String) autoSubmitButton).length() == 0) )
		{
			ElementId elemId;
			if( autoSubmitButton instanceof ElementId )
			{
				elemId = (ElementId) autoSubmitButton;
			}
			else
			{
				elemId = new SimpleElementId((String) autoSubmitButton);
			}
			autoSubmitRenderer = new AutoSubmission(this, elemId);
		}
	}

	public void setPlaceholderText(String placeholderText)
	{
		valueState.setPlaceholderText(placeholderText);
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		if( noLabel )
		{
			return null;
		}
		return super.getNestedRenderable();
	}

	public boolean isNoLabel()
	{
		return noLabel;
	}

	public void setNoLabel(boolean noLabel)
	{
		this.noLabel = noLabel;
	}

}
