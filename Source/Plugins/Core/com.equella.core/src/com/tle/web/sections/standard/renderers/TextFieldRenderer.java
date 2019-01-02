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

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.google.common.base.Strings;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.js.generic.statement.AssignAsFunction;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlTextFieldState;
import com.tle.web.sections.standard.model.HtmlValueState;

@SuppressWarnings("nls")
public class TextFieldRenderer extends AbstractTextFieldRenderer implements JSValueComponent, JSDisableable
{
	private int maxLength;
	private int size;
	private boolean password;
	private boolean hidden;

	public TextFieldRenderer(HtmlValueState state)
	{
		super(state, "text");
		this.password = false;
	}

	public TextFieldRenderer(HtmlTextFieldState state)
	{
		super(state, "text");
		this.password = state.isPassword();
		this.size = state.getSize();
		this.maxLength = state.getMaxLength();
	}

	@Override
	protected String getType()
	{
		if( hidden )
		{
			return "hidden";
		}
		if( password )
		{
			return "password";
		}
		return super.getType();
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);

		attrs.put("value", valueState.getValue());

		if( maxLength > 0 )
		{
			attrs.put("maxlength", Integer.toString(maxLength));
		}

		if( size > 0 )
		{
			attrs.put("size", Integer.toString(size));
		}

		if( !editable )
		{
			attrs.put("readonly", "readonly");
		}

		if( autocompleteDisabled )
		{
			attrs.put("autocomplete", "off");
		}

		final String placeholderText = valueState.getPlaceholderText();
		if( !Strings.isNullOrEmpty(placeholderText) )
		{
			attrs.put("placeholder", placeholderText);
		}
	}

	@Override
	public JSExpression createGetExpression()
	{
		return PropertyExpression.create(new ElementByIdExpression(this), "value");
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new DefaultDisableFunction(this);
	}

	@Override
	public JSCallable createSetFunction()
	{
		return new AssignAsFunction(createGetExpression());
	}

	@Override
	public JSCallable createResetFunction()
	{
		return new AssignAsFunction(createGetExpression(), PropertyExpression.create(new ElementByIdExpression(this),
			"defaultValue"));
	}

	public void setPassword(boolean password)
	{
		this.password = password;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public void setMaxLength(int maxLength)
	{
		this.maxLength = maxLength;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}
}
