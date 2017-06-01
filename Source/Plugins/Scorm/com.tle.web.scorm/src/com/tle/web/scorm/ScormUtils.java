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

package com.tle.web.scorm;

public final class ScormUtils
{
	public static final String ATTACHMENT_TYPE = "scorm";
	public static final String ATTACHMENT_RESOURCE_TYPE = "scormres";
	public static final String MIME_TYPE = "equella/scorm-package";

	/**
	 * @param instanceVersion cannot be null
	 * @return true if string starts with 1.0, 1.1, 1.2, otherwise false
	 */
	public static boolean isPreSCORM2004(String instanceVersion)
	{
		return instanceVersion.startsWith("1.0") || instanceVersion.startsWith("1.1")
			|| instanceVersion.startsWith("1.2");
	}

	private ScormUtils()
	{
		throw new Error();
	}
}
