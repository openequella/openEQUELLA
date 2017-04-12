package com.tle.core.connectors.service;

import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

public final class ExternalContentSortTypeKeys
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(ExternalContentSortTypeKeys.class);

	public static String get(String status)
	{
		return helper.key("sort." + status); //$NON-NLS-1$
	}

	public static String get(ExternalContentSortType sort)
	{
		return get(sort.name().toLowerCase());
	}

	private ExternalContentSortTypeKeys()
	{
		throw new Error();
	}
}
