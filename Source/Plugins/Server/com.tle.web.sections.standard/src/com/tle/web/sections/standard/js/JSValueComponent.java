package com.tle.web.sections.standard.js;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

public interface JSValueComponent
{
	/**
	 * @return Javascript to get the value of this component
	 */
	JSExpression createGetExpression();

	/**
	 * @return Javascript to set the value of this component
	 */
	JSCallable createSetFunction();

	/**
	 * @return Javascript to clear the value of this component
	 */
	JSCallable createResetFunction();
}
