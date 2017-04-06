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
		filters = new PluginTracker<FilestoreContentFilter>(pluginService, getClass(), "contentFilter", null,
			new PluginTracker.ExtensionParamComparator("order")).setBeanKey("bean");
	}
}
