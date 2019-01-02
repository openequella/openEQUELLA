/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
