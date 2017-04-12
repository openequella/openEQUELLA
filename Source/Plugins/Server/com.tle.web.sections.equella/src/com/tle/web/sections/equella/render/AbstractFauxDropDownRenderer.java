package com.tle.web.sections.equella.render;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Throwables;
import com.tle.common.Check;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.HiddenInput;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSMutableListComponent;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.AbstractElementRenderer;

@SuppressWarnings("nls")
public abstract class AbstractFauxDropDownRenderer extends AbstractElementRenderer
	implements
		JSMutableListComponent,
		JSDisableable
{
	private final ElementId hiddenId;

	protected final List<Option<?>> options;
	protected final Set<String> selectedValues;
	protected final boolean multiple;

	protected final SimpleFunction clickFunc;

	public AbstractFauxDropDownRenderer(HtmlListState state)
	{
		super(state);
		multiple = state.isMultiple();
		options = state.getOptions();
		selectedValues = state.getSelectedValues();
		hiddenId = state;
		ScriptVariable valueVar = new ScriptVariable("val");
		JSStatements changeBody = Js.statement(Jq.$val(hiddenId, valueVar));

		JSHandler lsChangeHandler = state.getHandler(JSHandler.EVENT_CHANGE);
		if( lsChangeHandler != null )
		{
			changeBody = StatementBlock.get(changeBody, lsChangeHandler);
		}
		clickFunc = new SimpleFunction(JSHandler.EVENT_CHANGE, state, changeBody, valueVar);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.render(new HiddenInput(hiddenId, state.getName(), getSelectedValue()));
	}

	@Override
	protected void processHandler(SectionWriter writer, Map<String, String> attrs, String event, JSHandler handler)
	{
		if( !event.equals(JSHandler.EVENT_CHANGE) )
		{
			super.processHandler(writer, attrs, event, handler);
		}
	}

	protected String getSelectedValue()
	{
		Iterator<String> iter = selectedValues.iterator();
		if( !iter.hasNext() )
		{
			return "";
		}
		return iter.next();
	}

	@Override
	public JSExpression createGetExpression()
	{
		return Jq.$val(hiddenId);
	}

	@Override
	public JSExpression createGetNameExpression()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSCallable createSetFunction()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSCallable createResetFunction()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSCallable createRemoveFunction()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSCallable createAddFunction()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSExpression createNotEmptyExpression()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public JSCallable createDisableFunction()
	{
		throw new UnsupportedOperationException();
	}

	protected Option<?> getSelectedOption()
	{
		HtmlListState state = (HtmlListState) getTagState();
		Set<String> selectedValueSet = state.getSelectedValues();
		List<Option<?>> optionList = state.getOptions();
		if( !Check.isEmpty(selectedValueSet) )
		{
			for( Option<?> option : optionList )
			{
				if( selectedValueSet.contains(option.getValue()) )
				{
					return option;
				}
			}
		}
		if( optionList.size() > 0 )
		{
			return optionList.get(0);
		}
		else
		{
			throw Throwables.propagate(new IllegalArgumentException("Must have at least 1 option!"));
		}
	}
}