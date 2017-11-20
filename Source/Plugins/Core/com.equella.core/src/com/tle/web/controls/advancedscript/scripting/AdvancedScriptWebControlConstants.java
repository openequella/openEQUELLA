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

package com.tle.web.controls.advancedscript.scripting;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public final class AdvancedScriptWebControlConstants
{
	/**
	 * A wrapper around the HttpServletRequest object which can only retrieve
	 * user defined request data
	 */
	public static final String REQUEST_MAP = "request";

	/**
	 * The id prefix of the current AdvancedScriptWebControl
	 */
	public static final String PREFIX = "prefix";

	/**
	 * Javascript statements that will submit the wizard form
	 */
	public static final String SUBMIT_JS = "submitJavascript";

	/**
	 * The preview URL base of the form
	 * http://myinstitution/preview/3432423432432/1/ (this includes the trailing
	 * slash)
	 */
	public static final String PREVIEW_URL_BASE = "previewUrlBase";

	/**
	 * This is *not* available in script. This is just a key for the attributes
	 * map.
	 */
	public static final String WIZARD_ID = "wizId";

	/**
	 * A prop bag to pass data from scripts/templates in this order: <br>
	 * onload -> onload client-side template -> onsubmit client-side template ->
	 * control body <br>
	 * Generally you would only set data in the server side onload script and
	 * read the data in the various templates although this is not enforced.
	 */
	public static final String ATTRIBUTES = "attributes";

	private AdvancedScriptWebControlConstants()
	{
		throw new Error();
	}
}
