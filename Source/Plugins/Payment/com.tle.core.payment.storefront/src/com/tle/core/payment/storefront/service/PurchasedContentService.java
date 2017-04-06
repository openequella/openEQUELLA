package com.tle.core.payment.storefront.service;

import java.util.List;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;

public interface PurchasedContentService
{
	List<PurchasedContent> enumerateForUser(String userId);

	PurchasedContent getForSourceItem(Store store, ItemId itemId);

	PurchasedContent getForLocalItem(ItemId itemId);

	PurchasedContent getForSourceUuid(Store store, String sourceUuid);
}
