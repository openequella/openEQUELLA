package com.tle.core.payment.storefront.index;

import static com.tle.core.payment.StoreFrontIndexFields.FIELD_CHECKED_OUT_BY;
import static com.tle.core.payment.StoreFrontIndexFields.FIELD_IS_PURCHASED;
import static com.tle.core.payment.StoreFrontIndexFields.FIELD_SUBSCRIPTION_END;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.payment.storefront.service.PurchasedContentService;
import com.tle.freetext.AbstractIndexingExtension;
import com.tle.freetext.IndexedItem;

@Bind
@Singleton
public class StoreFrontIndexer extends AbstractIndexingExtension
{
	@Inject
	private PurchaseService purchaseService;
	@Inject
	private PurchasedContentService purchasedContentService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		Document itemdoc = indexedItem.getItemdoc();

		List<String> users = indexedItem.getAttribute(PurchasedContent.class);

		if( !Check.isEmpty(users) )
		{
			for( String user : users )
			{
				itemdoc.add(new Field(FIELD_CHECKED_OUT_BY, user, Field.Store.NO, Field.Index.NOT_ANALYZED));
			}
		}
		List<PurchaseItem> list = indexedItem.getAttribute(PurchaseItem.class);

		if( !Check.isEmpty(list) )
		{
			PurchaseItem latest = null;
			for( PurchaseItem pi : list )
			{
				if( pi.getSubscriptionEndDate() == null )
				{
					continue;
				}
				if( latest == null || pi.getSubscriptionEndDate().after(latest.getSubscriptionEndDate()) )
				{
					latest = pi;
				}
			}
			if( latest != null )
			{
				String subscriptionEndDate = new UtcDate(latest.getSubscriptionEndDate()).format(Dates.ISO);
				itemdoc.add(new Field(FIELD_SUBSCRIPTION_END, subscriptionEndDate, Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			}
			itemdoc.add(new Field(FIELD_IS_PURCHASED, "true", Field.Store.NO, Field.Index.NOT_ANALYZED));
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
						ItemId itemId = ItemId.fromKey(indexedItem.getItemIdKey());
						PurchasedContent purchasedContent = purchasedContentService.getForLocalItem(itemId);
						if( purchasedContent != null )
						{
							List<String> users = purchaseService.enumerateCheckoutByforItem(new ItemId(purchasedContent
								.getSourceItemUuid(), purchasedContent.getSourceItemVersion()));
							indexedItem.setAttribute(PurchasedContent.class, users);

							if( purchaseService.isPurchased(itemId.getUuid()) )
							{
								List<PurchaseItem> list = purchaseService.enumerateForItem(itemId);
								indexedItem.setAttribute(PurchaseItem.class, list);
							}
						}
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
