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

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

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

public class TextAreaRenderer extends AbstractElementRenderer implements JSDisableable, JSValueComponent
{
	private int rows;
	private int cols;
	private boolean spellCheck;
	private final HtmlValueState valueState;

	public TextAreaRenderer(HtmlValueState state)
	{
		super(state);
		this.valueState = state;
	}

	public TextAreaRenderer(HtmlTextFieldState state)
	{
		this((HtmlValueState) state);
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public void setCols(int cols)
	{
		this.cols = cols;
	}
	
	public boolean isSpellCheck() 
	{
		return spellCheck;
	}

	public void setSpellCheck(boolean spellCheck) 
	{
		this.spellCheck = spellCheck;
	}

	@Override
	protected String getTag()
	{
		return "textarea"; //$NON-NLS-1$
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		if( rows > 0 )
		{
			attrs.put("rows", Integer.toString(rows)); //$NON-NLS-1$
		}
		if( cols > 0 )
		{
			attrs.put("cols", Integer.toString(cols)); //$NON-NLS-1$
		}
		if(!spellCheck)
		{
			attrs.put("spellcheck", "false");
		}
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		String value = valueState.getValue();
		if( value != null )
		{
			writer.writeText(value);
		}
	}

	@Override
	public JSExpression createGetExpression()
	{
		return PropertyExpression.create(new ElementByIdExpression(this), "value"); //$NON-NLS-1$
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
			"defaultValue")); //$NON-NLS-1$
	}
}
