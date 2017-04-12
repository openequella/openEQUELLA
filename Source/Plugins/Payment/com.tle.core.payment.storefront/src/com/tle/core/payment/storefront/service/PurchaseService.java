package com.tle.core.payment.storefront.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreHarvestableItemBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;

public interface PurchaseService
{
	/**
	 * Also resumes suspended items that the store says is ok
	 * 
	 * @param store
	 * @param item
	 */
	void downloadHarvestableItem(Store store, StoreHarvestableItemBean item);

	boolean downloadUpdatedHarvestableItems(Store store, Date lastHarvest);

	boolean suspendAllExpiredItems(Store store, Date lastHarvest);

	void suspendExpiredItem(Store store, String sourceItemUuid);

	/**
	 * @param itemUuid Local item UUID
	 * @return
	 */
	boolean isPurchased(String itemUuid);

	/**
	 * @param items
	 * @return
	 */
	Set<Item> filterNonPurchased(Set<Item> items);

	/**
	 * @param itemUuid Store's item UUID
	 * @return
	 */
	boolean isSourcePurchased(Store store, String itemUuid);

	List<PurchaseItem> enumerateForItem(ItemId itemId);

	List<PurchaseItem> enumerateForSourceItem(String uuid);

	/**
	 * Do not call this, this only exists for the scheduled task. Use
	 * startCheckDownloadableContentAndCheckSubscriptions instead.
	 */
	void checkDownloadableContentAndCheckSubscriptions();

	void startCheckDownloadableContentAndCheckSubscriptions();

	Purchase createPurchase(OrderStorePart part, StoreTransactionBean transaction);

	boolean downloadNewHarvestableItems(Store store);

	void resumeExistingItem(Store store, String sourceItemUuid);

	boolean resumeAllExistingItems(Store store);

	List<String> enumerateCheckoutByforItem(ItemId itemId);

	void startCheckCurrentOrders();

	PurchaseItem getPurchaseItem(String purchaseItemUuid);
}
