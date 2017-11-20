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

package com.tle.web.sections.standard.model;

import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.renderers.TextAreaRenderer;
import com.tle.web.sections.standard.renderers.TextFieldRenderer;

/**
 * The State for single text value {@code Section}s and {@code Renderers}.
 * <p>
 * Along with the text string, we also store whether or not it is editable.
 * 
 * @see TextFieldRenderer
 * @see TextAreaRenderer
 * @author jmaginnis
 */
public class HtmlValueState extends HtmlComponentState
{
	private String value;
	private String placeholderText;

	public HtmlValueState()
	{
		super(RendererConstants.TEXTFIELD);
	}

	public HtmlValueState(String defaultRenderer)
	{
		super(defaultRenderer);
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public String getPlaceholderText()
	{
		return placeholderText;
	}

	public void setPlaceholderText(String placeholderText)
	{
		this.placeholderText = placeholderText;
	}
}
