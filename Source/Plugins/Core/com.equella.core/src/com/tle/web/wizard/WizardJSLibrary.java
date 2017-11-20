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

package com.tle.web.wizard;

import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.js.generic.function.IncludeFile;

public final class WizardJSLibrary
{
	static
	{
		PluginResourceHandler.init(WizardJSLibrary.class);
	}

	@PlugURL("scripts/wizardctrl.js")
	private static String URL_LIBRARY;

	public static final IncludeFile INCLUDE = new IncludeFile(URL_LIBRARY);

	private WizardJSLibrary()
	{
		throw new Error();
	}
}
