package com.tle.web.navigation;

import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.TagState;

/**
 * @author aholland
 */
@Bind(BreadcrumbService.class)
@Singleton
public class BreadcrumbServiceImpl implements BreadcrumbService
{
	private PluginTracker<BreadcrumbProvider> providers;

	@Override
	public TagState getSearchCollectionCrumb(SectionInfo info, String collectionUuid)
	{
		return getProvider(SEARCH_COLLECTION).getBreadcrumb(info,
			Collections.singletonMap("collectionUuid", collectionUuid));
	}

	@Override
	public TagState getViewItemCrumb(SectionInfo info, Item item)
	{
		return getProvider(VIEW_ITEM).getBreadcrumb(info, Collections.singletonMap("item", item));
	}

	@Override
	public TagState getContributeCrumb(SectionInfo info)
	{
		return getProvider(CONTRIBUTE).getBreadcrumb(info, new HashMap<String, Object>());
	}

	protected BreadcrumbProvider getProvider(String type)
	{
		BreadcrumbProvider crumbProvider = providers.getBeanMap().get(type);
		if( crumbProvider == null )
		{
			throw new Error("No " + type + " breadcrumb provider found!");
		}
		return crumbProvider;
	}

	@Inject
	public void setProviders(PluginService pluginService)
	{
		providers = new PluginTracker<BreadcrumbProvider>(pluginService, "com.tle.web.sections.equella", "breadcrumb",
			"type");
		providers.setBeanKey("class");
	}
}
