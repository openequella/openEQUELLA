package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.statement.AssignAsFunction;

/**
 * This interface is an abstraction of anything that behaves as a function. A
 * JSCallable does not necessarily have to represent an actual Javascript
 * function, it could for example represent a javascript assign statement.
 * 
 * @author jmaginnis
 * @see AssignAsFunction
 */
@NonNullByDefault
public interface JSCallable extends JSFunction
{
	String getExpressionForCall(RenderContext info, JSExpression... params);
}
