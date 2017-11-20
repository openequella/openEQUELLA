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

package com.tle.web.viewitem.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.viewitem.FilestoreContentFilter;

@Bind(FileFilterService.class)
@Singleton
public class FileFilterServiceImpl implements FileFilterService
{
	private PluginTracker<FilestoreContentFilter> filters;

	@Override
	public List<FilestoreContentFilter> getFilters()
	{
		return filters.getBeanList();
	}

	@SuppressWarnings("nls")
	@Inject
	public void setPluginService(PluginService pluginService)
	{
		filters = new PluginTracker<FilestoreContentFilter>(pluginService, "com.tle.web.viewitem", "contentFilter", null,
			new PluginTracker.ExtensionParamComparator("order")).setBeanKey("bean");
	}
}
