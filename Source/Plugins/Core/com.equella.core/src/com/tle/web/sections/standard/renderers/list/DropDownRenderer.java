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

package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.SelectNotEmpty;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSMutableListComponent;
import com.tle.web.sections.standard.js.modules.SelectModule;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.HtmlMutableListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.AbstractElementRenderer;

@SuppressWarnings("nls")
public class DropDownRenderer extends AbstractElementRenderer implements JSMutableListComponent, JSDisableable
{
	protected List<Option<?>> options;
	private Set<String> selectedValues;
	protected int size;
	private final boolean multiple;
	private boolean selectAll;
	private final boolean grouped;

	public DropDownRenderer(HtmlListState state)
	{
		super(state);
		multiple = state.isMultiple();
		options = state.getOptions();
		selectedValues = state.getSelectedValues();
		grouped = state.isGrouped();
	}

	public DropDownRenderer(HtmlMutableListState state)
	{
		super(state);
		selectAll = true;
		options = state.getOptions();
		multiple = true;
		grouped = state.isGrouped();
	}

	protected boolean isSelectAll()
	{
		return selectAll;
	}

	@Override
	protected void prepareLastAttributes(SectionWriter writer, Map<String, String> attrs)
	{
		if( isSelectAll() )
		{
			writer.bindHandler(JSHandler.EVENT_PRESUBMIT, attrs, new StatementHandler(SelectModule.SELECT_ALL, this));
		}
		if( multiple )
		{
			attrs.put("multiple", "multiple");
			if( size == 0 )
			{
				size = 10;
			}
		}
		if( size > 0 )
		{
			attrs.put("size", Integer.toString(size));
		}
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		if( grouped )
		{
			writer.endTag("optgroup");
		}
		super.writeEnd(writer);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		if( grouped )
		{
			String gn = "";
			for( Option<?> opt : options )
			{
				String currentGroup = opt.getGroupName();

				// Write new group if first or different
				if( !currentGroup.equals(gn) )
				{
					if( !gn.equals("") )
					{
						writer.endTag("optgroup");
					}
					gn = currentGroup;
					writer.writeTag("optgroup", "label", currentGroup);
				}
				writeOption(writer, opt);
			}
		}
		else
		{
			// Write options
			for( Option<?> opt : options )
			{
				if( !isIncluded(opt, selectedValues) )
				{
					continue;
				}
				writeOption(writer, opt);
			}
		}
	}

	private void writeOption(SectionWriter writer, Option<?> opt) throws IOException
	{
		Map<String, String> attrs = new LinkedHashMap<String, String>();
		boolean dontSelect = isDontSelect();

		final String val = opt.getValue();
		attrs.put("value", val);
		if( !dontSelect && selectedValues.contains(val) )
		{
			attrs.put("selected", "selected");
		}
		else
		{
			attrs.remove("selected");
		}
		if( opt.isDisabled() )
		{
			attrs.put("disabled", "disabled");
		}
		else
		{
			attrs.remove("disabled");
		}
		writer.writeTag("option", attrs);
		if( opt.isNameHtml() )
		{
			writer.write(opt.getName());
		}
		else
		{
			writer.writeText(opt.getName());
		}
		writer.endTag("option");
		writer.write('\n');
	}

	protected boolean isDontSelect()
	{
		return isSelectAll();
	}

	protected boolean isIncluded(Option<?> nameValue, Set<String> selectedValues)
	{
		return true;
	}

	@Override
	protected String getTag()
	{

		return "select";
	}

	@Override
	public JSExpression createGetExpression()
	{
		if( !multiple )
		{
			return libraryCall(SelectModule.SELECTED_VALUE);
		}
		return libraryCall(SelectModule.SELECTED_VALUES);
	}

	private JSExpression libraryCall(JSCallable call)
	{
		return new FunctionCallExpression(call, this);
	}

	@Override
	public JSExpression createGetNameExpression()
	{
		if( !multiple )
		{
			return libraryCall(SelectModule.SELECTED_TEXT);
		}
		return libraryCall(SelectModule.SELECTED_TEXTS);
	}

	@Override
	public JSCallable createSetFunction()
	{
		if( !multiple )
		{
			return new PrependedParameterFunction(SelectModule.SET_VALUE, this);
		}
		throw new UnsupportedOperationException("Currently unsupported for multiple selections");
	}

	@Override
	public JSCallable createResetFunction()
	{
		return new PrependedParameterFunction(SelectModule.RESET_SELECTED_VALUES, this);
	}

	@Override
	public JSCallable createRemoveFunction()
	{
		return new PrependedParameterFunction(SelectModule.REMOVE_SELECTED, this);
	}

	@Override
	public JSCallable createAddFunction()
	{
		return new PrependedParameterFunction(SelectModule.ADD_OPTION, this);
	}

	@Override
	public JSExpression createNotEmptyExpression()
	{
		return new SelectNotEmpty(this);
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new DefaultDisableFunction(this); //$NON-NLS-1$
	}

	@Override
	public JSCallable createSetAllFunction()
	{
		throw new UnsupportedOperationException("Not yet");
	}
}
