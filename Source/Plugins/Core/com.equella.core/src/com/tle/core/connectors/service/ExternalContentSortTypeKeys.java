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

package com.tle.core.connectors.service;

import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

public final class ExternalContentSortTypeKeys
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(ExternalContentSortTypeKeys.class);

	public static String get(String status)
	{
		return helper.key("sort." + status); //$NON-NLS-1$
	}

	public static String get(ExternalContentSortType sort)
	{
		return get(sort.name().toLowerCase());
	}

	private ExternalContentSortTypeKeys()
	{
		throw new Error();
	}
}
