/*
 * Created on 25/01/2006
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
		contributors = new PluginTracker<MenuContributor>(pluginService, getClass(), "menuContributor",
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
