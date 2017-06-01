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

package com.tle.web.scripting.impl;

import com.tle.common.scripting.objects.LoggingScriptObject;
import com.tle.common.util.Logger;

/**
 * @author aholland
 */
public class LoggingScriptWrapper extends AbstractScriptWrapper implements LoggingScriptObject
{
	private static final long serialVersionUID = 1L;

	private final Logger logger;

	public LoggingScriptWrapper(Logger logger)
	{
		this.logger = logger;
	}

	@Override
	public void log(String text)
	{
		logger.info(text);
	}
}
