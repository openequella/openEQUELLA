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

package com.tle.web.scripting.advanced.objects;

import java.util.Locale;
import java.util.TimeZone;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'lang' variable in script. Locale, Language and TimeZone
 * functions are all relative to the current user.
 * 
 * @author aholland
 */
public interface RegionalScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "lang"; //$NON-NLS-1$

	/**
	 * Get a language string from the language pack from it's key value.
	 * 
	 * @param key The key of the string
	 * @return The value of the string (if any)
	 */
	String getString(String key);

	/**
	 * Gets the current user's locale based on: (in this order) - the user's
	 * preference (in "edit my details") - the user's browser setting - the
	 * default for the server
	 * 
	 * @return A java.util.Locale object
	 */
	Locale getLocale();

	/**
	 * Gets the current user's time zone based on: (in this order) - the user's
	 * preference (in "edit my details") - the institution default - the default
	 * for the server
	 * 
	 * @return A java.util.TimeZone object
	 */
	TimeZone getTimeZone();
}
