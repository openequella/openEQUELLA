package com.tle.core.payment.storefront.service;

import java.util.Currency;
import java.util.List;
import java.util.Map;

import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.storefront.CurrencyTotal;
import com.tle.core.payment.storefront.OrderWorkflowInfo;
import com.tle.core.payment.storefront.StoreTotal;
import com.tle.core.payment.storefront.exception.OrderValueChangedException;

/**
 * @author Aaron
 */
public interface OrderService
{
	String KEY_ORDER_ITEM = "orderitem"; //$NON-NLS-1$
	String KEY_COMMENT = "comment"; //$NON-NLS-1$

	Order getShoppingCart();

	/**
	 * throws InvalidDataException (an unchecked exception)
	 */
	void addToShoppingCart(Order shoppingCart, Store store, OrderItem orderItem);

	void removeStoreFromShoppingCart(Order shoppingCart, Store store);

	void removeFromShoppingCart(Order shoppingCart, Store store, OrderItem orderItem);

	Order submitShoppingCart(Order shoppingCart, String comment);

	Order submitOrder(Order order, String comment);

	/**
	 * throws InvalidDataException (an unchecked exception)
	 */
	Order rejectOrder(Order order, String comment);

	Order redraftOrder(Order order);

	void deleteOrder(Order order);

	boolean isEmpty(Order order);

	StoreCheckoutBean submitToStore(Order order, Store store) throws OrderValueChangedException;

	/**
	 * Pays a PORTION of an order. Turns all order items for a single store into
	 * a purchase.
	 * 
	 * @param order
	 * @param transaction As returned from store transaction endpoint
	 * @return
	 */
	Purchase payOrderPart(OrderStorePart part, StoreTransactionBean transaction);

	Order get(long id);

	Order getByUuid(String uuid);

	List<Order> getByUuidsWithStatus(List<String> uuids, Order.Status status);

	/**
	 * Returns null if there is no section for the supplied store
	 * 
	 * @param order
	 * @param store
	 * @return
	 */
	OrderStorePart getStorePart(Order order, Store store);

	/**
	 * Returns null if item not present
	 * 
	 * @param order
	 * @param store
	 * @param itemUuid
	 * @return
	 */
	OrderItem getOrderItem(Order order, Store store, String itemUuid);

	/**
	 * NOTE: this only calculates the totals based on the previously calculated
	 * values of the items in the order. This total may have changed due to
	 * changes in pricing on the store side.
	 * 
	 * @param order
	 * @return
	 */
	Map<Currency, CurrencyTotal> calculateCurrencyTotals(Order order);

	/**
	 * This method may modify OrderItems with correct price values.
	 * 
	 * @param store
	 * @param order
	 * @param pricing Recalculate order prices with information from supplied
	 *            pricing
	 * @return the recalculated price for the supplied store. Note: currency may
	 *         be null if the whole shop order is free.
	 * @throws OrderValueChangedException only thrown in fresh is supplied and
	 *             the order value is different to expected
	 */
	StoreTotal calculateStoreTotal(Order order, Store store, StorePricingInformationBean pricing);

	/**
	 * NOTE: this only calculates the totals based on the previously calculated
	 * values of the items in the order. This total may have changed due to
	 * changes in pricing on the store side.
	 * 
	 * @param order
	 * @param store
	 * @return
	 */
	StoreTotal softCalculateStoreTotal(Order order, Store store);

	boolean isTotallyFree(Order order);

	int getItemCount(Order order);

	// Workflow

	/**
	 * List all orders that the current user has submitted in the past. They may
	 * now be awaiting payment, awaiting approval or have been rejected.
	 * 
	 * @return
	 */
	List<Order> enumerateSent();

	/**
	 * List all orders that the current user can approve
	 * 
	 * @return
	 */
	List<Order> enumerateApprovalForUser();

	/**
	 * List all orders that the current user can pay
	 * 
	 * @return
	 */
	List<Order> enumeratePaymentForUser();

	/**
	 * Where does this order go next?
	 * 
	 * @param order
	 * @return
	 */
	OrderWorkflowInfo getOrderProcess(Order order);

	boolean isCurrentUserAnApprover();

	boolean isCurrentUserAPayer();

	/**
	 * @param wait Waits 5 seconds, there is a chance our order could
	 *            immediately be ready
	 */
	void startCheckCurrentOrders(boolean wait);

	/**
	 * To be used only by the scheduled task. Use startCheckCurrentOrders
	 * instead.
	 */
	void checkCurrentOrders();

	boolean canView(Order order);
}
