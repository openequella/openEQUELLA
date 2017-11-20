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

package com.tle.web;

@SuppressWarnings("nls")
public final class DebugSettings
{
	private static boolean autoTestMode;
	private static boolean debuggingMode;
	private static boolean debugAaron;

	static
	{
		autoTestMode = (System.getProperty("equella.autotest") != null);
		debuggingMode = (System.getProperty("equella.debug") != null);
		debugAaron = (System.getProperty("equella.debugaaron") != null);
	}

	public static boolean isAutoTestMode()
	{
		return autoTestMode;
	}

	public static boolean isDebuggingMode()
	{
		return debuggingMode || debugAaron;
	}

	public static boolean isDebugAaron()
	{
		return debugAaron;
	}

	private DebugSettings()
	{
		throw new Error();
	}
}
