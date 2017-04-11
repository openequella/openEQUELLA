package com.tle.web.sections.js;

import com.tle.annotation.NonNullByDefault;

/**
 * A JSAssignable represents a function that can be assigned to something. Which
 * is why it also extends JSExpression.
 * <p>
 * For a class to implement this interface, it MUST allow the following snippet
 * of javascript to be correct, where ${expr} is the results of getExpression()
 * and the number of parameters is 2.
 * 
 * <pre>
 * var ref = ${expr};
 * ref(1,2);
 * </pre>
 * 
 * @author jolz
 */
@NonNullByDefault
public interface JSAssignable extends JSFunction, JSExpression
{
	// Nothing to do here
}
