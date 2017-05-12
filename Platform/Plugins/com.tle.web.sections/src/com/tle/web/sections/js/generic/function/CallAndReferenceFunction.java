package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.statement.AssignStatement;
import com.tle.web.sections.render.PreRenderable;

public final class CallAndReferenceFunction
{
	@SuppressWarnings("nls")
	public static JSCallAndReference get(final JSFunction func, ElementId id)
	{
		if( func instanceof JSCallAndReference )
		{
			JSCallAndReference callRef = (JSCallAndReference) func;
			if( !id.isStaticId() || callRef.isStatic() )
			{
				return (JSCallAndReference) func;
			}
		}
		if( func instanceof JSCallable )
		{
			return new PassThroughFunction("ref", id, (JSCallable) func);
		}

		final ScriptVariable var = new ScriptVariable("fref", id); //$NON-NLS-1$
		ExternallyDefinedFunction extFunc = new ExternallyDefinedFunction("fref", id, -1, new PreRenderable()
		{
			@Override
			public void preRender(PreRenderContext info)
			{
				info.addStatements(new AssignStatement(var, func));
			}
		});
		extFunc.setParamNumFunc(func);
		return extFunc;
	}

	private CallAndReferenceFunction()
	{
		throw new Error();
	}
}
