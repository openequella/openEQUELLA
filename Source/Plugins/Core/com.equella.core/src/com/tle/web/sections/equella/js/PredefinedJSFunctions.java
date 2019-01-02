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

package com.tle.web.sections.equella.js;

import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public final class PredefinedJSFunctions
{
	private static final IncludeFile JS = new IncludeFile(ResourcesService.getResourceHelper(
		PredefinedJSFunctions.class).url("scripts/predefinedfunctions.js"));

	public static final ExternallyDefinedFunction ENSURE_INPUT = new ExternallyDefinedFunction("ensureInput", JS);

	public static final ExternallyDefinedFunction ENSURE_SELECTED = new ExternallyDefinedFunction("ensureSelected", JS);

	private PredefinedJSFunctions()
	{
		throw new Error();
	}
}
