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
		links = new PluginTracker<TopbarLink>(pluginService, getClass(), "topbarLink", "class",
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
