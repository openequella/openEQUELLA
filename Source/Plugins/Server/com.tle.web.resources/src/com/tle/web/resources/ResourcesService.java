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

package com.tle.web.resources;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.java.plugin.Plugin;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.PluginService;
import com.tle.core.services.ApplicationVersion;

@SuppressWarnings("nls")
@NonNullByDefault
public class ResourcesService
{
	@Nullable
	/* @LazyNonNull */
	private static ResourcesService instance;
	@Nullable
	/* @LazyNonNull */
	private String baseUrl;

	@Inject
	private PluginService pluginService;
	@Inject
	private InstitutionService institutionService;

	@PostConstruct
	public void afterPropertiesSet()
	{
		instance = this; // NOSONAR

		String build = ApplicationVersion.get().getMmr();
		baseUrl = "p/r/" + build + '/';
	}

	public String getUrl(String pluginId, String path)
	{
		StringBuilder b = new StringBuilder(75);
		b.append(baseUrl);
		b.append(pluginId);
		b.append('/');
		b.append(path);
		return b.toString();
	}

	public PluginResourceHelper getHelper(Object pluginObj)
	{
		String pluginId;
		if( pluginObj instanceof String )
		{
			pluginId = (String) pluginObj;
		}
		else if( pluginObj instanceof Plugin )
		{
			pluginId = ((Plugin) pluginObj).getDescriptor().getId();
		}
		else
		{
			pluginId = pluginService.getPluginForObject(pluginObj).getDescriptor().getId();
		}

		return new PluginResourceHelper(this, institutionService, pluginId, pluginService);
	}

	public static PluginResourceHelper getResourceHelper(Object pluginObj)
	{
		return instance.getHelper(pluginObj);
	}
}
