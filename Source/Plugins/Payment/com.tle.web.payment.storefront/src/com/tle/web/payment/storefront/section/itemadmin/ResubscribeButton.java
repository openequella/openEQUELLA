package com.tle.web.payment.storefront.section.itemadmin;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.core.guice.Bind;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.user.CurrentUser;
import com.tle.web.itemlist.item.ItemListEntry;
import com.tle.web.itemlist.item.ItemlikeListEntryExtension;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.viewitem.RootShopViewItemSection;
import com.tle.web.payment.shop.section.viewitem.ShopItemPurchaseDetailsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * @author dustin
 */
@Bind
public class ResubscribeButton extends AbstractPrototypeSection<Object>
	implements
		ItemlikeListEntryExtension<Item, ItemListEntry>
{
	@PlugKey("itemadmin.resubscribe")
	private static Label RESUBSCRIBE_LABEL;

	@EventFactory
	private EventGenerator events;

	@Inject
	private PurchaseService purchaseService;
	@Inject
	private TLEAclManager aclService;

	@Override
	public ProcessEntryCallback<Item, ItemListEntry> processEntries(RenderContext context, List<ItemListEntry> entries,
		ListSettings<ItemListEntry> listSettings)
	{
		if( CurrentUser.wasAutoLoggedIn() )
		{
			return null;
		}
		return new ProcessEntryCallback<Item, ItemListEntry>()
		{
			@Override
			public void processEntry(ItemListEntry entry)
			{
				final Item item = entry.getItem();
				if( !purchaseService.isPurchased(item.getUuid()) )
				{
					return;
				}
				List<PurchaseItem> purchaseItems = purchaseService.enumerateForItem(item.getItemId());
				if( purchaseItems == null || purchaseItems.size() < 1 )
				{
					return;
				}

				PurchaseItem purchaseItem = purchaseItems.get(0);
				if( !isResubscribeable(purchaseItems) )
				{
					return;
				}

				Purchase purchase = purchaseItem.getPurchase();
				// ensure PRIV_ACCESS_SHOPPING_CART priv
				if( aclService.filterNonGrantedObjects(
					Collections.singleton(StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART),
					Collections.singleton(purchase.getStore())).isEmpty() )
				{
					return;
				}

				HtmlComponentState link = new HtmlComponentState(RESUBSCRIBE_LABEL, events.getNamedHandler("viewItem",
					purchaseItem.getUuid()));

				entry.addRatingAction(new ButtonRenderer(link));
			}
		};
	}

	private boolean isResubscribeable(List<PurchaseItem> purchases)
	{
		boolean is = false;

		for( PurchaseItem purchase : purchases )
		{
			if( purchase.getUsers() == 0 && purchase.getSubscriptionStartDate() == null )
			{
				return false;
			}
			if( purchase.getSubscriptionStartDate() != null )
			{
				is = true;
			}
		}
		return is;
		// This is correct but might be in-efficient
	}

	@EventHandlerMethod
	public void viewItem(SectionInfo info, String purchaseItemUuid)
	{
		final PurchaseItem purchaseItem = purchaseService.getPurchaseItem(purchaseItemUuid);

		final SectionInfo fwd = info.createForward(ShopConstants.URL_VIEWITEM);
		final RootShopViewItemSection view = fwd.lookupSection(RootShopViewItemSection.class);
		final ShopItemPurchaseDetailsSection purchase = fwd.lookupSection(ShopItemPurchaseDetailsSection.class);
		view.setItem(fwd, purchaseItem.getPurchase().getStore().getUuid(), purchaseItem.getCatalogueUuid(),
			purchaseItem.getSourceItemUuid());

		// Setting the start date to this item's subscription end date would be
		// nice, but that's not as easy as it sounds
		purchase.setValues(fwd, true, purchaseItem.getUsers(), null, null);

		info.forwardAsBookmark(fwd);
		// The next page will handle any problems for us (the item has been
		// deleted from the catalogue etc)
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
