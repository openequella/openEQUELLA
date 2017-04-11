package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.js.JSCallable;

public final class HelperFunction extends RuntimeRefFunction
{
	public static final HelperFunction TRIGGER = new HelperFunction(HelperType.T_TRIGGER);

	private enum HelperType
	{
		T_TRIGGER
	}

	private HelperType type;

	private HelperFunction(HelperType type)
	{
		this.type = type;
	}

	@Override
	protected JSCallable getRealFunction(RenderContext info)
	{
		HeaderHelper helper = info.getHelper();
		switch( type )
		{
			case T_TRIGGER:
				return helper.getTriggerEventFunction();
		}
		throw new UnsupportedOperationException();
	}
}
