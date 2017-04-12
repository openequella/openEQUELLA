package com.tle.web.sections.equella;

import com.tle.beans.item.ItemStatus;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

public final class ItemStatusKeys
{
	private static PluginResourceHelper helper = ResourcesService.getResourceHelper(ItemStatusKeys.class);

	public static String get(String status)
	{
		return helper.key("itemstatus." + status); //$NON-NLS-1$
	}

	public static String get(ItemStatus status)
	{
		return get(status.name().toLowerCase());
	}

	private ItemStatusKeys()
	{
		throw new Error();
	}
}
