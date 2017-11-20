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

package com.tle.web.navigation;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.template.section.MenuContributor;

/**
 * @author Nicholas Read
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class MenuService
{
	private PluginTracker<MenuContributor> contributors;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		contributors = new PluginTracker<MenuContributor>(pluginService, "com.tle.web.sections.equella", "menuContributor",
			"menuContributorClass");
		contributors.setBeanKey("menuContributorClass");
	}

	public void clearCachedData()
	{
		for( MenuContributor contributor : contributors.getBeanList() )
		{
			contributor.clearCachedData();
		}
	}

	public PluginTracker<MenuContributor> getContributors()
	{
		return contributors;
	}
}
