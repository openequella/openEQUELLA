package com.tle.web.wizard.controls;

import com.tle.web.sections.js.JSAssignable;

public interface SimpleValueControl extends WebControl
{
	JSAssignable createEditFunction();

	JSAssignable createValueFunction();

	JSAssignable createTextFunction();

	JSAssignable createResetFunction();
}
