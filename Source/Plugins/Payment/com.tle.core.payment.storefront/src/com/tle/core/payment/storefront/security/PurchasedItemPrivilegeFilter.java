package com.tle.core.payment.storefront.security;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Sets;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.security.DomainObjectPrivilegeFilterExtension;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class PurchasedItemPrivilegeFilter implements DomainObjectPrivilegeFilterExtension
{
	@Inject
	private PurchaseService purchaseService;

	private static final Set<String> DISALLOWED = Collections.unmodifiableSet(Sets.newHashSet("CLONE_ITEM",
		"SUSPEND_ITEM", "RESUME_ITEM", "SET_TIERS_FOR_ITEM", "VIEW_TIERS_FOR_ITEM", "MANAGE_CATALOGUE",
		"VIEW_SALES_FOR_ITEM", "VIEW_TIERS_FOR_ITEM", "NEWVERSION_ITEM", "DELETE_ITEM"));

	private Set<String> getDisallowed()
	{
		// May change in future (ie be dynamic)
		return DISALLOWED;
	}

	@Override
	public void filterPrivileges(Object domainObject, Set<String> privileges)
	{
		if( domainObject instanceof Item )
		{
			final Item item = (Item) domainObject;
			if( purchaseService.isPurchased(item.getUuid()) )
			{
				privileges.removeAll(getDisallowed());
			}
		}
	}

	@Override
	public <T> void filterPrivileges(Map<T, Map<String, Boolean>> objectToPrivileges)
	{
		final Set<Item> items = Sets.newHashSet();
		for( T obj : objectToPrivileges.keySet() )
		{
			if( obj instanceof Item )
			{
				items.add((Item) obj);
			}
		}
		final Set<Item> purchased = purchaseService.filterNonPurchased(items);
		for( Item item : purchased )
		{
			final Map<String, Boolean> privs = objectToPrivileges.get(item);
			for( String disallowed : getDisallowed() )
			{
				final Boolean granted = privs.get(disallowed);
				if( granted != null && granted )
				{
					privs.remove(disallowed);
				}
			}
		}
	}
}
