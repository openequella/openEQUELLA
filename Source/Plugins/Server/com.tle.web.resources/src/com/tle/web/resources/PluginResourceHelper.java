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
