package com.tle.web.sections.standard.js;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

public interface JSListComponent extends JSValueComponent
{
	JSExpression createNotEmptyExpression();

	JSExpression createGetNameExpression();

	JSCallable createSetAllFunction();
}
