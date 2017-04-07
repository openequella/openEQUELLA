package com.tle.web.sections.js.generic;

import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;

/**
 * A handler which always returns false in order to stop further event handling.
 */
public class OverrideHandler extends StatementHandler
{
	public OverrideHandler()
	{
		// nothing
	}

	public OverrideHandler(JSStatements... statements)
	{
		super(statements);
	}

	public OverrideHandler(JSStatements statements)
	{
		super(statements);
	}

	public OverrideHandler(JSCallable callable, Object... args)
	{
		super(callable, args);
	}

	public OverrideHandler(JSHandler handler, JSStatements statements)
	{
		super(handler, statements);
	}

	@Override
	public boolean isOverrideDefault()
	{
		return true;
	}
}
