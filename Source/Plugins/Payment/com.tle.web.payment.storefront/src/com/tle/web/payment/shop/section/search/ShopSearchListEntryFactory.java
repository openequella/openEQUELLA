package com.tle.web.payment.shop.section.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.guice.Bind;
import com.tle.core.payment.beans.store.StoreCatalogueAttachmentBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.payment.shop.ShopConstants;
import com.tle.web.payment.shop.section.viewitem.RootShopViewItemSection;
import com.tle.web.payment.shop.service.ShopMoneyLabelService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.generic.InfoBookmark;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;

/**
 * @author Dustin
 */
@Bind
@Singleton
public class ShopSearchListEntryFactory
{
	@PlugKey("store.search.modified")
	private static Label modifiedLabel;
	@PlugKey("store.search.price")
	private static Label priceLabel;
	@PlugKey("store.search.free")
	private static Label freeLabel;
	@PlugKey("store.search.subscription")
	private static Label subscriptionLabel;
	@PlugKey("shop.search.subscriptiontype")
	private static String KEY_SUBTYPE;

	@Inject
	private Provider<ShopSearchListEntry> entryProvider;
	@Inject
	private ShopMoneyLabelService moneyLabelService;

	public ShopSearchListEntry createListEntry(SectionInfo info, Store store, String catUuid,
		StoreCatalogueItemBean storeCatalogueItemBean)
	{

		ShopSearchListEntry listItem = entryProvider.get();
		final SectionInfo fwd = info.createForward(ShopConstants.URL_VIEWITEM);
		final RootShopViewItemSection view = fwd.lookupSection(RootShopViewItemSection.class);
		view.setItem(fwd, store.getUuid(), catUuid, storeCatalogueItemBean.getUuid());

		final String name;
		I18NStrings nameStrings = storeCatalogueItemBean.getNameStrings();
		if( nameStrings == null )
		{
			I18NString nameString = storeCatalogueItemBean.getName();
			if( nameString == null )
			{
				name = storeCatalogueItemBean.getUuid();
			}
			else
			{
				name = nameString.toString();
			}
		}
		else
		{
			name = nameStrings.asI18NString(storeCatalogueItemBean.getUuid()).toString();
		}

		listItem.setTitle(new HtmlLinkState(new TextLabel(name), new InfoBookmark(fwd)));

		I18NString description = storeCatalogueItemBean.getDescription();

		listItem.setDescription(new TextLabel(description == null ? "" : description.toString()));

		List<StoreCatalogueAttachmentBean> attachments = storeCatalogueItemBean.getAttachments();
		for( StoreCatalogueAttachmentBean attachment : attachments )
		{
			Map<String, String> links = (Map<String, String>) attachment.get("links");
			String thumbUrl = links.get("thumbnail");
			if( thumbUrl != null )
			{
				listItem.addThumbnail(new ImageRenderer(thumbUrl, new TextLabel(attachment.getDescription())));
			}
		}

		List<MetadataEntry> metadata = new ArrayList<MetadataEntry>();

		// shouldn't be null...
		if( storeCatalogueItemBean.getModifiedDate() != null )
		{
			metadata.add(new StdMetadataEntry(modifiedLabel, JQueryTimeAgo.timeAgoTag(storeCatalogueItemBean
				.getModifiedDate())));
		}

		StorePurchaseTierBean purchaseTier = storeCatalogueItemBean.getPurchaseTier();
		if( storeCatalogueItemBean.isFree() )
		{
			metadata.add(new StdMetadataEntry(priceLabel, new LabelRenderer(freeLabel)));
		}
		else
		{
			if( purchaseTier != null )
			{
				metadata.add(new StdMetadataEntry(priceLabel, new LabelRenderer(moneyLabelService.getLabel(
					purchaseTier.getPrice(), 1L))));
			}

			StoreSubscriptionTierBean subscriptionTier = storeCatalogueItemBean.getSubscriptionTier();
			if( subscriptionTier != null )
			{
				StorePriceBean priceBean = subscriptionTier.getPrices().get(0); // Cheapest?
				String duration = priceBean.getPeriod().getName();
				metadata.add(new StdMetadataEntry(subscriptionLabel, new LabelRenderer(new KeyLabel(KEY_SUBTYPE,
					moneyLabelService.getLabel(priceBean, 1L).getText(), duration))));
			}
		}
		listItem.setMetadata(metadata);
		return listItem;
	}
}
