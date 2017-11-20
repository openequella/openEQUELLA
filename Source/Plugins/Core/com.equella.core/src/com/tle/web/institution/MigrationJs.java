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

package com.tle.web.institution;

import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.jquery.JQueryCookie;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.jquery.libraries.JQueryTimer;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public class MigrationJs
{
	static
	{
		PluginResourceHandler.init(MigrationJs.class);
	}

	@PlugURL("js/migration.js")
	private static String URL_MIGRATIONJS;

	private static final IncludeFile MIGRATION_JS = new IncludeFile(URL_MIGRATIONJS, JQueryProgression.PRERENDER,
		JQueryTimer.PRERENDER, AjaxGenerator.AJAX_LIBRARY, JQueryCookie.PRERENDER);

	public static final JSCallAndReference SETUP_PROGRESS = new ExternallyDefinedFunction("setupProgress", 2,
		MIGRATION_JS);

	public static final JSCallAndReference HANDLER_DISPATCHER = new ExternallyDefinedFunction("handlerDispatcher", 2,
		MIGRATION_JS);

	public static final JSCallAndReference WAIT_FOR = new ExternallyDefinedFunction("waitFor", MIGRATION_JS);

	public static final JSCallAndReference SETUP_PROGRESS_DIALOG = new ExternallyDefinedFunction("setupProgressDialog",
		MIGRATION_JS);

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	protected MigrationJs()
	{
		// not to be instantiated, hence nothing to construct except a token
		// hidden constructor to silence Sonar
	}

}
