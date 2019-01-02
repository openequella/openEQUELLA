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

package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.expression.StringExpression;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.ListOption;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSListComponent;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.FormValuesLibrary;

@SuppressWarnings("nls")
@NonNullByDefault
public class BooleanListRenderer implements JSDisableable, SectionRenderable, JSListComponent
{
	private final HtmlListState state;
	@Nullable
	private JSHandler changeHandler;
	@Nullable
	private SimpleFunction changeFunc;

	public BooleanListRenderer(HtmlListState listState)
	{
		this.state = listState;
		JSHandler lsChangeHandler = state.getHandler(JSHandler.EVENT_CHANGE);
		if( lsChangeHandler != null )
		{
			changeFunc = new SimpleFunction(JSHandler.EVENT_CHANGE, state, lsChangeHandler);
			changeHandler = new StatementHandler(changeFunc);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		throw new Error("Cannot be rendered directly, need to call renderOptionList()");
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		throw new Error("Cannot be rendered directly, need to call renderOptionList()");
	}

	@SuppressWarnings("unchecked")
	public <T> List<ListOption<T>> renderOptionList(RenderContext context)
	{
		List<ListOption<T>> options = Lists.newArrayList();
		state.setBeenRendered(true);
		Set<String> values = state.getSelectedValues();
		List<Option<?>> rawOptions = state.getOptions();
		String name = state.getName();
		int i = 0;
		for( Option<?> option : rawOptions )
		{
			HtmlBooleanState bstate = new HtmlBooleanState();
			bstate.setDisabled(state.isDisabled() || option.isDisabled());
			bstate.setName(name);
			bstate.setId(name + '_' + i);
			String value = option.getValue();
			bstate.setValue(value);
			bstate.setChecked(values.contains(value));
			bstate.setRendererType(state.isMultiple() ? RendererConstants.CHECKBOX : RendererConstants.RADIO);
			options.add(new ListOption<T>((Option<T>) option, bstate));
			if( changeHandler != null )
			{
				bstate.setClickHandler(changeHandler);
			}
			i++;
		}
		return options;
	}

	@Override
	public JSExpression createNotEmptyExpression()
	{
		return new FunctionCallExpression(FormValuesLibrary.IS_SOME_CHECKED, new StringExpression(state.getName()));
	}

	@Override
	public JSExpression createGetNameExpression()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSExpression createGetExpression()
	{
		return new FunctionCallExpression(state.isMultiple() ? FormValuesLibrary.GET_CHECKED_VALUES
			: FormValuesLibrary.GET_CHECK_VALUE, new StringExpression(state.getName()));
	}

	@Override
	public JSCallable createSetFunction()
	{
		throw new UnsupportedOperationException("Not yet"); //$NON-NLS-1$
	}

	@Override
	public JSCallable createResetFunction()
	{
		throw new UnsupportedOperationException("Not yet"); //$NON-NLS-1$
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new PrependedParameterFunction(FormValuesLibrary.SET_ALL_DISABLED_STATE, new StringExpression(
			state.getName()));
	}

	public HtmlListState getListState()
	{
		return state;
	}

	@Override
	public JSCallable createSetAllFunction()
	{
		ScriptVariable b = new ScriptVariable("b");
		JSStatements body = Js.call_s(FormValuesLibrary.SET_ALL_CHECKED_STATE, state.getName(), b);
		if( changeFunc != null )
		{
			body = StatementBlock.get(body, Js.call_s(changeFunc));
		}

		return new SimpleFunction("setAll", state, body, b);
	}

}
