package com.tle.web.resources;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.java.plugin.Plugin;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.plugins.PluginService;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.UrlService;

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
	private UrlService urlService;

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

		return new PluginResourceHelper(this, urlService, pluginId, pluginService);
	}

	public static PluginResourceHelper getResourceHelper(Object pluginObj)
	{
		return instance.getHelper(pluginObj);
	}
}
