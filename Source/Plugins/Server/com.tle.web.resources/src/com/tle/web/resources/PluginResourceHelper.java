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

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.plugins.PluginService;
import com.tle.core.services.UrlService;

public class PluginResourceHelper
{
	public static final String KEY_RESOURCEHELPER = "$RSRCHELPER$"; //$NON-NLS-1$

	private final ResourcesService resourcesService;
	private final UrlService urlService;
	private final PluginService pluginService;

	private final String pluginId;

	public PluginResourceHelper(ResourcesService service, UrlService urlService, String pluginId,
		PluginService pluginService)
	{
		resourcesService = service;
		this.urlService = urlService;
		this.pluginId = pluginId;
		this.pluginService = pluginService;
	}

	public String instUrl(String path)
	{
		return urlService.institutionalise(path);
	}

	public String key(String key)
	{
		return pluginId + '.' + key;
	}

	public String getString(String localKey, Object... values)
	{
		return CurrentLocale.get(pluginId + '.' + localKey, values);
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(String clazzName)
	{
		return (T) pluginService.getBean(pluginId, clazzName);
	}

	public String url(String resource)
	{
		return resourcesService.getUrl(pluginId, resource);
	}

	public String plugUrl(String pluginId, String resource)
	{
		return resourcesService.getUrl(pluginId, resource);
	}

	public String pluginId()
	{
		return pluginId;
	}
}
