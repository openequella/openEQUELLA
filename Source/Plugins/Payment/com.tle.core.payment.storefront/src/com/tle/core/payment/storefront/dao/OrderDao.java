package com.tle.core.payment.storefront.dao;

import java.util.List;

import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderHistory;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author Aaron
 */
public interface OrderDao extends GenericInstitutionalDao<Order, Long>
{
	int MAX_CURSORY_ENQUIRY = 100;

	Order getShoppingCart();

	void deleteAll();

	List<Order> enumerateSent(String userId);

	List<Order> enumerateApproval();

	List<Order> enumeratePayment();

	List<Order> enumeratePaymentAndPending();

	Order getByUuid(String uuid);

	List<Order> getByUuidsWithStatus(List<String> uuids, Order.Status status);

	List<String> getUnfinishedOrderUuids();

	void deleteAllForCreator(String userId);

	void updateCreator(String fromUserId, String toUserId);

	void updateLastActionUser(String fromUserId, String toUserId);

	List<OrderHistory> findHistoryForUser(String userId);
}
