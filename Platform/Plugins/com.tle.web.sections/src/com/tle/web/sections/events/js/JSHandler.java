package com.tle.web.sections.events.js;

import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSStatements;

@SuppressWarnings("nls")
public interface JSHandler extends JSStatements
{
	String EVENT_BEFOREUNLOAD = "beforeunload";
	String EVENT_CLICK = "click";
	String EVENT_CHANGE = "change";
	String EVENT_BLUR = "blur";
	String EVENT_FOCUS = "focus";
	String EVENT_READY = "ready";
	String EVENT_PRESUBMIT = "presubmit";
	String EVENT_VALIDATE = "validate";
	String EVENT_LOAD = "load";
	String EVENT_KEYPRESS = "keypress";
	String EVENT_KEYUP = "keyup";

	JSCallAndReference getW3CHandler();

	JSCallAndReference getHandlerFunction();

	JSHandler addValidator(JSStatements validator);

	JSStatements getStatements();

	JSStatements getValidators();

	JSHandler addStatements(JSStatements statements);

	JSBookmarkModifier getModifier();

	boolean isOverrideDefault();
}
