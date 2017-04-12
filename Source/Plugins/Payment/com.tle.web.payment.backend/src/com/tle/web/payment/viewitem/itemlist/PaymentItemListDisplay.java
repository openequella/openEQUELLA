package com.tle.web.payment.viewitem.itemlist;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.guice.Bind;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.i18n.BundleCache;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;

@Bind
public class PaymentItemListDisplay extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("search.summary.tiers")
	private static Label LABEL_TIERS;
	@PlugKey("search.summary.tier.free")
	private static Label LABEL_FREE;

	@Inject
	private PricingTierService tierService;
	@Inject
	private TLEAclManager aclService;
	@Inject
	private BundleCache bundleCache;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		ListSettings<ItemListEntry> listSettings)
	{
		// TODO: Is the following actually a bug? Should it be calculating the
		// privilege for each item (in batch up here) then doing the check in
		// processEntry below?
		if( aclService.filterNonGrantedPrivileges(PaymentConstants.PRIV_VIEW_TIERS_FOR_ITEM).isEmpty() )
		{
			return null;
		}

		final List<Item> items = AbstractItemlikeListEntry.getItems(entries);
		final Map<Item, PricingTierAssignment> tiers = tierService.getAssignmentsForItems(items);

		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				final Item item = entry.getItem();

				final PricingTierAssignment pta = tiers.get(item);
				if( pta != null )
				{
					final List<Label> tierLabels = Lists.newArrayList();
					if( pta.isFreeItem() )
					{
						tierLabels.add(LABEL_FREE);
					}
					final PricingTier purchaseTier = pta.getPurchasePricingTier();
					if( purchaseTier != null )
					{
						tierLabels.add(new BundleLabel(purchaseTier.getName(), bundleCache));
					}
					final PricingTier subscriptionTier = pta.getSubscriptionPricingTier();
					if( subscriptionTier != null )
					{
						tierLabels.add(new BundleLabel(subscriptionTier.getName(), bundleCache));
					}
					entry.addDelimitedMetadata(LABEL_TIERS, tierLabels);
				}
			}
		};
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@Override
	public String getItemExtensionType()
	{
		return null;
	}
}
