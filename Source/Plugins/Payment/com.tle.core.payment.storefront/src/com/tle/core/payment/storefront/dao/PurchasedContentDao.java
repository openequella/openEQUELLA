package com.tle.core.payment.storefront.dao;

import java.util.List;
import java.util.Set;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchasedContent;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * Tracks which items are purchased
 * 
 * @author Aaron
 */
public interface PurchasedContentDao extends GenericInstitutionalDao<PurchasedContent, Long>
{
	void deleteAll();

	/**
	 * Lists content from all stores
	 * 
	 * @param userId
	 * @return
	 */
	List<PurchasedContent> enumerateForUser(String userId);

	PurchasedContent getForSourceItem(Store store, ItemId itemId);

	/**
	 * Lists content from all stores
	 * 
	 * @param itemId
	 * @return
	 */
	PurchasedContent getForLocalItem(ItemId itemId);

	/**
	 * @param store
	 * @param sourceUuid The UUID of the item on the store side.
	 * @return
	 */
	PurchasedContent getForSourceUuid(Store store, String sourceUuid);

	/**
	 * @param uuids local item UUIDs
	 * @return A subset of uuids (the items which were purchased)
	 */
	Set<String> getPurchased(Set<String> uuids);

	/**
	 * Get all purchased items (either outright or subscription) that are in
	 * Live or Review states.
	 * 
	 * @param store
	 * @return
	 */
	List<PurchasedContent> getLivePurchases(Store store);
}
