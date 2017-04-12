package com.tle.web.sections.result.util;

import com.tle.beans.item.IItem;
import com.tle.web.i18n.BundleCache;

public class ItemNameLabel extends BundleLabel
{
	public ItemNameLabel(IItem<?> item, BundleCache bundleCache)
	{
		super(item.getName(), item.getUuid(), bundleCache);
	}
}
