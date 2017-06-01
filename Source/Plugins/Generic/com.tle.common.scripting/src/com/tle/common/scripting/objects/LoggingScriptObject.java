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

package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'logger' variable in script
 * 
 * @author aholland
 */
public interface LoggingScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "logger"; //$NON-NLS-1$

	/**
	 * Logs a message in the Resource Centre log files. This is logged at the
	 * INFO level and is controlled by the
	 * com.tle.common.scripting.service.ScriptingService logger
	 * 
	 * @param text The text to log
	 */
	void log(String text);
}
