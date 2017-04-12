package com.tle.core.payment.storefront.dao;

import java.util.List;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.PurchaseItem;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * A PurchaseItem is a row on a Purchase
 * 
 * @author Aaron
 */
public interface PurchaseItemDao extends GenericDao<PurchaseItem, Long>
{
	List<PurchaseItem> enumerateForPurchaser(String buyerId);

	List<PurchaseItem> enumerateForItem(ItemId itemId);

	List<PurchaseItem> enumerateForSourceItem(String uuid);

	/**
	 * Returns all PurchaseItems which are within the current subscription
	 * period or are outright purchases.
	 * 
	 * @param store
	 * @param sourceItemUuid
	 * @return
	 */
	List<PurchaseItem> findActivePurchases(Store store, String sourceItemUuid);

	PurchaseItem get(String uuid);
}
