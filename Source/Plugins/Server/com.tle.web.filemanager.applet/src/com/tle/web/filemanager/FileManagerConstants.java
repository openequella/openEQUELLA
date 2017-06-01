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

package com.tle.web.filemanager;

import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
public final class FileManagerConstants
{
	public static final String FILEMANAGER_APPLET_JAR_URL = ResourcesService.getResourceHelper(
		FileManagerConstants.class).plugUrl("com.tle.web.filemanager.applet", "filemanager.jar");

	private FileManagerConstants()
	{
		throw new Error();
	}
}
