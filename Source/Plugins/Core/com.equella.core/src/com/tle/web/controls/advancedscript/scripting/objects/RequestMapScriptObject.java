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

package com.tle.web.controls.advancedscript.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'request' variable in script (OnSubmit script only). Allows
 * you to retrieve request parameters of any inputs/selects you have rendered in
 * your display template. All input/select names must be prefixed with ${prefix}
 * 
 * @author aholland
 */
public interface RequestMapScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "request"; //$NON-NLS-1$

	/**
	 * Returns a single value posted by this control. Note that you must use
	 * ${prefix} on the name of any input you are going to read on the server.
	 * E.g. &lt;input type="hidden" name="${prefix}myInput" value="test"> will
	 * be read as request.get('myInput') in script
	 * 
	 * @param key The un-prefixed name of the value to read
	 * @return A single value, or null if nothing with the specified key was
	 *         posted
	 */
	String get(String key);

	/**
	 * Returns a list of values posted by this control. This is used in the case
	 * of checkboxes or multiple input fields with the same name. Note that you
	 * must use ${prefix} on the name of any input you are going to read on the
	 * server. E.g. &lt;input type="hidden" name="${prefix}myInput"
	 * value="test"><br>
	 * &lt;input type="hidden" name="${prefix}myInput" value="test2"> will be
	 * read as request.getList('myInput') in script
	 * 
	 * @param key The un-prefixed name of the values to read
	 * @return An array of values, or null if nothing with the specified key was
	 *         posted
	 */
	String[] getList(String key);
}
