package com.tle.web.sections.standard.js.impl;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.DoNothing;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.standard.js.JSDisableable;

public class CombinedDisableable implements JSDisableable
{
	protected List<JSCallable> disablers = new ArrayList<JSCallable>();
	protected List<JSDisableable> lazyDisablers = new ArrayList<JSDisableable>();
	private boolean isUsed;
	private final ElementId elementId;

	public CombinedDisableable(ElementId id)
	{
		this.elementId = id;
	}

	public CombinedDisableable(ElementId id, JSDisableable... disablers)
	{
		this.elementId = id;
		addDisablers(disablers);
	}

	public void addDisabler(JSDisableable disabler)
	{
		lazyDisablers.add(disabler);
		checkLazy();
	}

	public void addDisablers(JSDisableable... disablers)
	{
		for( JSDisableable disabler : disablers )
		{
			if( disabler != null )
			{
				lazyDisablers.add(disabler);
			}
		}
		checkLazy();
	}

	private void checkLazy()
	{
		if( isUsed )
		{
			for( JSDisableable lazyDisabler : lazyDisablers )
			{
				disablers.add(lazyDisabler.createDisableFunction());
			}
			lazyDisablers.clear();
		}
	}

	@Override
	public JSCallable createDisableFunction()
	{
		isUsed = true;
		checkLazy();
		return new RuntimeFunction()
		{
			@Override
			protected JSCallable createFunction(RenderContext info)
			{
				if( disablers.isEmpty() )
				{
					return DoNothing.FUNCTION;
				}
				if( disablers.size() == 1 )
				{
					return disablers.get(0);
				}
				ScriptVariable dis = new ScriptVariable("dis"); //$NON-NLS-1$
				StatementBlock body = new StatementBlock();
				for( JSCallable disableCall : disablers )
				{
					body.addStatements(new FunctionCallStatement(disableCall, dis));
				}
				return new SimpleFunction("setDisabled", elementId, body, dis); //$NON-NLS-1$
			}
		};
	}
}
