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

package com.tle.web.viewitem.treeviewer.js;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.js.generic.function.IncludeFile;

@SuppressWarnings("nls")
public final class TreeLibrary
{
	private static final PluginResourceHelper URL_HELPER = ResourcesService.getResourceHelper(TreeLibrary.class);

	public static final IncludeFile INCLUDE = new IncludeFile(URL_HELPER.url("scripts/treelib.js"), new IncludeFile(
		"scripts/utf8.js"));

	private TreeLibrary()
	{
		throw new Error();
	}
}
