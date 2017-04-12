package com.tle.core.payment.operation;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.guice.Bind;
import com.tle.core.payment.operation.ChangeCatalogueState.ChangeCatalogueAssignment;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.operations.DuringSaveOperation;
import com.tle.core.workflow.operations.DuringSaveOperationGenerator;

/**
 * When NewVersioning and item we need to clone the assigned pricing tiers and
 * white/blacklisted catalogues
 * 
 * @author Aaron
 */
@Bind
public class CloneCataloguesAndTiersOperationGenerator extends AbstractWorkflowOperation
	implements
		DuringSaveOperationGenerator
{
	@Inject
	private PricingTierService tierService;
	@Inject
	private CatalogueService catService;

	private ChangeCatalogueState catState;
	private ChangeTierState tierState;

	@Override
	public boolean execute()
	{
		final ItemPack<Item> itemPack = getItemPack();
		final Item originalItem = itemPack.getOriginalItem();
		final Item item = itemPack.getItem();

		// We only clone stuff if a new version is done, not a clone
		if( originalItem.getUuid().equals(item.getUuid()) && originalItem.getVersion() < item.getVersion() )
		{
			final List<ChangeCatalogueAssignment> cats = Lists.newArrayList();
			for( CatalogueAssignment catAss : catService.listCataloguesForItem(originalItem) )
			{
				cats.add(new ChangeCatalogueAssignment(true, catAss.getCatalogue().getId(), catAss.isBlacklisted()));
			}
			if( !cats.isEmpty() )
			{
				catState = new ChangeCatalogueState(cats);
			}

			final PricingTierAssignment tierAss = tierService.getPricingTierAssignmentForItem(originalItem.getItemId());
			if( tierAss != null )
			{
				PricingTier purchasePricingTier = tierAss.getPurchasePricingTier();
				PricingTier subscriptionPricingTier = tierAss.getSubscriptionPricingTier();
				final String purchaseUuid = (purchasePricingTier == null ? null : purchasePricingTier.getUuid());
				final String subscriptionUuid = (subscriptionPricingTier == null ? null : subscriptionPricingTier
					.getUuid());
				tierState = new ChangeTierState(tierAss.isFreeItem(), purchaseUuid, subscriptionUuid);
			}
		}
		return false;
	}

	@Override
	public Collection<DuringSaveOperation> getDuringSaveOperation()
	{
		Collection<DuringSaveOperation> ops = Lists.newArrayList();
		if( catState != null )
		{
			ops.add(new CloneCatalogues(catState));
		}
		if( tierState != null )
		{
			ops.add(new CloneTiers(tierState));
		}
		return ops;
	}
}
