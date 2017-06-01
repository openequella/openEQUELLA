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

package com.tle.web.appletcommon;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class AppletWebCommon
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(AppletWebCommon.class);
	public static final IncludeFile INCLUDE = new IncludeFile(resources.url("scripts/writeapplet.js"));
	public static final String PARAMETER_PREFIX = "jnlp.";

	/**
	 * writeAppletTags
	 * 
	 * @param placeholder $('#somediv')
	 * @param jarurl 'http://[]/p/r/com.tle.web.recipientapplet/selector.jar'
	 * @param mainclass 'com.tle.web.myapplet.AppletLauncher'
	 * @param locale 'en-AU'
	 * @param endpoint 'http://lebowski:8080/Shiny/my/'
	 * @param height 300px
	 * @param width 50px
	 * @param options ObjectExpression
	 * @param id applet ID
	 */
	public static final ExternallyDefinedFunction WRITE_APPLET = new ExternallyDefinedFunction("writeAppletTags",
		INCLUDE);

	private AppletWebCommon()
	{
		throw new Error();
	}
}