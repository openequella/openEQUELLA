/*
 * Copyright 2017 Apereo
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
