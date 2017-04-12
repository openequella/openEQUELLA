package com.tle.web.payment.storefront.section;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.StandardItemList;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;

@Bind
public class PurchasedItemList extends StandardItemList
{
	@Override
	protected Set<String> getExtensionTypes()
	{
		return ImmutableSet.of(ItemlikeListEntryExtension.TYPE_STANDARD, "purchasedsubsearch");
	}
}
