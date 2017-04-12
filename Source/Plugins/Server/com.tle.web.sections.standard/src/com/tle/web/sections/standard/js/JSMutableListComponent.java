package com.tle.web.sections.standard.js;

import com.tle.web.sections.js.JSCallable;

public interface JSMutableListComponent extends JSListComponent
{
	JSCallable createAddFunction();

	JSCallable createRemoveFunction();
}
