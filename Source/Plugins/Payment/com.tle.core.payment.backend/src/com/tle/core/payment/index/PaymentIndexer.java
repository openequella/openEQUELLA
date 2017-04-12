package com.tle.core.payment.index;

import static com.tle.core.payment.PaymentIndexFields.FIELD_BLACKLISTED;
import static com.tle.core.payment.PaymentIndexFields.FIELD_FREE_TIER;
import static com.tle.core.payment.PaymentIndexFields.FIELD_PURCHASE_TIER;
import static com.tle.core.payment.PaymentIndexFields.FIELD_SUBSCRIPTION_TIER;
import static com.tle.core.payment.PaymentIndexFields.FIELD_WHITELISTED;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.tle.common.payment.entity.CatalogueAssignment;
import com.tle.common.payment.entity.PricingTier;
import com.tle.common.payment.entity.PricingTierAssignment;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.payment.service.CatalogueService;
import com.tle.core.payment.service.PricingTierService;
import com.tle.freetext.AbstractIndexingExtension;
import com.tle.freetext.IndexedItem;

@Bind
@Singleton
public class PaymentIndexer extends AbstractIndexingExtension
{
	@Inject
	private PricingTierService tierService;
	@Inject
	private CatalogueService catalogueService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		PricingTierAssignment pricingTier = indexedItem.getAttribute(PricingTierAssignment.class);
		Document itemdoc = indexedItem.getItemdoc();
		if( pricingTier != null )
		{
			boolean free = pricingTier.isFreeItem();
			PricingTier purTier = pricingTier.getPurchasePricingTier();
			PricingTier subTier = pricingTier.getSubscriptionPricingTier();

			if( purTier != null )
			{
				itemdoc
					.add(new Field(FIELD_PURCHASE_TIER, purTier.getUuid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
			}

			if( subTier != null )
			{
				itemdoc.add(new Field(FIELD_SUBSCRIPTION_TIER, subTier.getUuid(), Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			}

			itemdoc.add(new Field(FIELD_FREE_TIER, Boolean.toString(free), Field.Store.YES, Field.Index.NOT_ANALYZED));
		}

		List<CatalogueAssignment> catalogueAssignments = indexedItem.getAttribute(CatalogueAssignment.class);
		for( CatalogueAssignment catAssignment : catalogueAssignments )
		{
			itemdoc.add(new Field(catAssignment.isBlacklisted() ? FIELD_BLACKLISTED : FIELD_WHITELISTED, catAssignment
				.getCatalogue().getUuid(), Field.Store.YES, Field.Index.NOT_ANALYZED));
		}
	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		// no slow
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		for( final IndexedItem indexedItem : items )
		{
			try
			{
				runAs.executeAsSystem(indexedItem.getInstitution(), new Callable<Void>()
				{
					@Override
					public Void call()
					{
						PricingTierAssignment tierAss = tierService.getPricingTierAssignmentForItem(indexedItem
							.getItemIdKey());
						List<CatalogueAssignment> catalogueAssignments = catalogueService
							.listCataloguesForItem(indexedItem.getItem());
						indexedItem.setAttribute(PricingTierAssignment.class, tierAss);
						indexedItem.setAttribute(CatalogueAssignment.class, catalogueAssignments);
						return null;
					}
				});
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}

		}
	}
}
