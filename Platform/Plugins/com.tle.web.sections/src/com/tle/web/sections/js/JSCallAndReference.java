package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;

/**
 * Interface that represents a callable and assignable javascript function.
 * <p>
 * For a class to implement this interface, it MUST allow the following snippet
 * of javascript to be correct, where ${expr} is the results of getExpression(),
 * ${call} is the result of getCallForExpression() and the number of parameters
 * is 2.
 * 
 * <pre>
 * var ref = ${expr};
 * ref(1,2);
 * ${call(1, 2)};
 * </pre>
 * <p>
 * JSCallAndReference could be considered as representing a standard javascript
 * function.
 * 
 * @author jolz
 */
@NonNullByDefault
public interface JSCallAndReference extends JSCallable, JSAssignable
{

	/**
	 * If it is static, the result of
	 * {@link #getExpression(com.tle.web.sections.events.RenderContext)} ,
	 * {@link #getExpressionForCall(com.tle.web.sections.events.RenderContext, JSExpression...)}
	 * and {@link #getNumberOfParams(com.tle.web.sections.events.RenderContext)}
	 * will never change. It means that
	 * {@link #getExpression(com.tle.web.sections.events.RenderContext)} can be
	 * called with a {@code null} {@code RenderContext}.
	 * 
	 * @return Whether or not this is static
	 */
	boolean isStatic();
}
