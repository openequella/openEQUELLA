package com.tle.core.payment.storefront.dao;

import java.util.List;

import com.tle.beans.item.ItemId;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * A Purchase contains many PurchaseItem rows and is basically a transaction (as
 * recorded on the storefront)
 * 
 * @author Aaron
 */
public interface PurchaseDao extends GenericInstitutionalDao<Purchase, Long>
{
	void deleteAll();

	List<String> enumerateCheckoutByforItem(ItemId itemId);

	void updateCheckoutUser(String fromUserId, String toUserId);

	void updatePaidByUser(String fromUserId, String toUserId);
}
