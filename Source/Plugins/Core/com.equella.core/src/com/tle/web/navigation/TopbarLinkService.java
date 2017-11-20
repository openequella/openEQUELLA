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
import com.tle.web.template.section.TopbarLink;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TopbarLinkService
{
	private PluginTracker<TopbarLink> links;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		links = new PluginTracker<TopbarLink>(pluginService, "com.tle.web.sections.equella", "topbarLink", "class",
			new PluginTracker.ExtensionParamComparator("order"));
		links.setBeanKey("class");

	}

	public PluginTracker<TopbarLink> getTopbarLinks()
	{
		return links;
	}

	public void clearCachedData()
	{
		for( TopbarLink link : links.getBeanList() )
		{
			link.clearCachedCount();
		}
	}
}
