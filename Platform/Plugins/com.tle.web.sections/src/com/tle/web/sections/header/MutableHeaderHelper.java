package com.tle.web.sections.header;

import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;

public interface MutableHeaderHelper extends HeaderHelper
{
	void setFormExpression(JSExpression formExpression);

	void setElementFunction(JSCallable elementFunction);

	boolean isSubmitFunctionsSet();

	// Lack of Platform branches fail
	void setSubmitFunctions(JSCallable submit, JSCallable submitNV, JSCallable submitEvent, JSCallable submitEventNV);

	void setSubmitFunctions(JSCallable submit, JSCallable submitNoValidation, JSCallable submitNoBlock,
		JSCallable submitEvent, JSCallable submitEventNoValidation, JSCallable submitEventNoBlock);

	void setTriggerEventFunction(JSCallAndReference triggerEventFunction);
}
