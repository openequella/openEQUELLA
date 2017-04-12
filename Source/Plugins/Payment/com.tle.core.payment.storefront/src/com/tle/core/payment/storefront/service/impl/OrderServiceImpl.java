package com.tle.core.payment.storefront.service.impl;

import static com.tle.common.security.SecurityConstants.getRecipientType;
import static com.tle.common.security.SecurityConstants.getRecipientValue;
import static com.tle.core.payment.storefront.constants.StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_APPROVAL;
import static com.tle.core.payment.storefront.constants.StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_PAYMENT;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.valuebean.ValidationError;
import com.dytech.edge.exceptions.InvalidDataException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.payment.storefront.entity.ApprovalsPaymentsSettings;
import com.tle.common.payment.storefront.entity.ApprovalsPaymentsSettings.ApprovalsPayments;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.Order.Status;
import com.tle.common.payment.storefront.entity.OrderHistory;
import com.tle.common.payment.storefront.entity.OrderItem;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.common.payment.storefront.entity.OrderStorePart.PaidStatus;
import com.tle.common.payment.storefront.entity.Purchase;
import com.tle.common.payment.storefront.entity.Store;
import com.tle.core.dao.user.TLEGroupDao;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.notification.NotificationService;
import com.tle.core.payment.beans.store.DecimalNumberBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePricingInformationBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.beans.store.StoreTransactionBean;
import com.tle.core.payment.storefront.CurrencyTotal;
import com.tle.core.payment.storefront.OrderWorkflowInfo;
import com.tle.core.payment.storefront.StoreTotal;
import com.tle.core.payment.storefront.TaxTotal;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.dao.OrderDao;
import com.tle.core.payment.storefront.exception.OrderValueChangedException;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.payment.storefront.service.PurchaseService;
import com.tle.core.payment.storefront.service.ShopService;
import com.tle.core.scheduler.ScheduledTask;
import com.tle.core.security.RunAsUser;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(OrderService.class)
@Singleton
public class OrderServiceImpl implements OrderService, UserChangeListener, ScheduledTask
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(OrderService.class);
	private static Logger LOGGER = Logger.getLogger(OrderServiceImpl.class);

	// Just a boolean, but included for code readability
	private enum Side
	{
		LHS, RHS
	}

	@Inject
	private OrderDao dao;
	@Inject
	private PurchaseService purchaseService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ShopService shopService;
	@Inject
	private TLEGroupDao groupDao;
	@Inject
	private NotificationService notificationService;
	@Inject
	private RunAsUser runAsUser;

	@RequiresPrivilege(priv = StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART)
	@Transactional
	@Override
	public Order getShoppingCart()
	{
		Order order = dao.getShoppingCart();
		if( order == null )
		{
			order = new Order();
			order.setUuid(UUID.randomUUID().toString());
			order.setInstitution(CurrentInstitution.get());
			order.setCreatedBy(CurrentUser.getUserID());
			order.setCreatedDate(new Date());
			dao.save(order);
		}
		return order;
	}

	/**
	 * throws InvalidDataException (an unchecked exception)
	 */
	@RequiresPrivilege(priv = StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART)
	@Transactional
	@Override
	public void addToShoppingCart(Order shoppingCart, Store store, OrderItem orderItem)
	{
		final List<ValidationError> errors = new ArrayList<ValidationError>();
		OrderStorePart storePart = getStorePart(shoppingCart, store);

		if( storePart == null )
		{
			storePart = new OrderStorePart();
			storePart.setUuid(UUID.randomUUID().toString());
			storePart.setStore(store);
			storePart.setOrder(shoppingCart);
			shoppingCart.getStoreParts().add(storePart);
		}

		OrderItem exists = getOrderItem(shoppingCart, store, orderItem.getItemUuid());
		if( exists != null )
		{
			errors.add(new ValidationError(KEY_ORDER_ITEM, resources.getString("cart.error.duplicate")));
			throw new InvalidDataException(errors);
		}

		orderItem.setUuid(UUID.randomUUID().toString());
		orderItem.setAddedDate(new Date());

		storePart.getOrderItems().add(orderItem);
		// Re-calc
		final StoreTotal totes = softCalculateStoreTotal(shoppingCart, store);
		storePart.setCurrency(totes.getCurrency());
		storePart.setPrice(totes.getLongValue());
		storePart.setTax(totes.getLongTaxValue());
		// A bit dodge
		for( String taxCode : totes.getTaxCodes() )
		{
			storePart.setTaxCode(taxCode);
		}
		dao.save(shoppingCart);
	}

	@RequiresPrivilege(priv = StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART)
	@Transactional
	@Override
	public void removeFromShoppingCart(Order shoppingCart, Store store, OrderItem orderItem)
	{
		final OrderStorePart storePart = getStorePart(shoppingCart, store);
		if( storePart != null )
		{
			final Iterator<OrderItem> it = storePart.getOrderItems().iterator();
			while( it.hasNext() )
			{
				final OrderItem i = it.next();
				if( orderItem.getItemUuid().equals(i.getItemUuid()) )
				{
					it.remove();
					break;
				}
			}

			if( storePart.getOrderItems().size() == 0 )
			{
				shoppingCart.getStoreParts().remove(storePart);
			}
			else
			{
				// Re-calc
				final StoreTotal totes = softCalculateStoreTotal(shoppingCart, store);
				storePart.setCurrency(totes.getCurrency());
				storePart.setPrice(totes.getLongValue());
				storePart.setTax(totes.getLongTaxValue());
				// A bit dodge
				for( String taxCode : totes.getTaxCodes() )
				{
					storePart.setTaxCode(taxCode);
				}
			}
			dao.save(shoppingCart);
		}
	}

	@RequiresPrivilege(priv = StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART)
	@Transactional
	@Override
	public void removeStoreFromShoppingCart(Order shoppingCart, Store store)
	{
		final Iterator<OrderStorePart> it = shoppingCart.getStoreParts().iterator();
		while( it.hasNext() )
		{
			final OrderStorePart osp = it.next();
			if( osp.getStore().getUuid().equals(store.getUuid()) )
			{
				it.remove();
				dao.save(shoppingCart);
				return;
			}
		}
	}

	@RequiresPrivilege(priv = StoreFrontConstants.PRIV_ACCESS_SHOPPING_CART)
	@Override
	public Order submitShoppingCart(Order order, String comment)
	{
		return submitOrderForReals(order, comment);
	}

	@Override
	public Order submitOrder(Order order, String comment)
	{
		return submitOrderForReals(order, comment);
	}

	@Transactional
	public Order submitOrderForReals(Order order, String comment)
	{
		final UserState userState = CurrentUser.getUserState();
		final Status status = order.getStatus();
		if( status == Status.CART || status == Status.APPROVAL || status == Status.REJECTED )
		{
			if( status == Status.CART )
			{
				// Was a shopping cart, but now a proper order, so we'll set the
				// created date
				order.setCreatedDate(new Date());
			}

			progressOrder(order, userState, comment);

			dao.save(order);
			return order;
		}
		// Bad state. Can't submit an order that isn't in approval state
		throw new RuntimeException(resources.getString("order.error.submitapprovebadstate", (Object) new Status[]{
				Status.CART, Status.APPROVAL, Status.REJECTED}));
	}

	private void progressOrder(Order order, UserState userState, String comment)
	{
		final OrderHistory historyItem = new OrderHistory();
		historyItem.setOrder(order);
		historyItem.setUserId(userState.getUserBean().getUniqueID());
		historyItem.setDate(new Date());
		historyItem.setComment(comment);

		final Status orderStatus = order.getStatus();

		final ApprovalsPaymentsSettings settings = configService.getProperties(new ApprovalsPaymentsSettings());
		final boolean owner = isOwner(order, userState);

		boolean checkPayer = false;
		boolean progressed = false;

		if( orderStatus == Status.CART || orderStatus == Status.REJECTED )
		{
			final Pair<String, String> approverRule = getApproverRule(userState, settings, Side.LHS, owner);
			if( approverRule != null )
			{
				final AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
				// If current user can also approve, then send to payer
				if( evaluator.evaluate(approverRule.getSecond(), userState, owner) )
				{
					checkPayer = true;
				}
				else
				{
					progressed = true;
					historyItem.setStatus(Status.APPROVAL);
					String approversExpr = approverRule.getSecond();
					order.setApproversExpression(approversExpr);
					notifyApproversOrPayers(StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_APPROVAL, approversExpr,
						order);
				}
			}
		}

		if( checkPayer || orderStatus == Status.APPROVAL )
		{
			final Pair<String, String> payerRule = getPayerRule(userState, settings, Side.LHS, owner);
			if( payerRule != null )
			{
				progressed = true;
				historyItem.setStatus(Status.PAYMENT);
				String payersExpr = payerRule.getSecond();
				order.setPayersExpression(payersExpr);
				notifyApproversOrPayers(StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_PAYMENT, payersExpr, order);
			}
			else if( checkPayer )
			{
				// TODO: refactor, it's a repeat of the approval code above
				final Pair<String, String> approverRule = getApproverRule(userState, settings, Side.LHS, owner);
				if( approverRule != null )
				{
					progressed = true;
					historyItem.setStatus(Status.APPROVAL);
					String approversExpr = approverRule.getSecond();
					order.setApproversExpression(approversExpr);
					notifyApproversOrPayers(StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_APPROVAL, approversExpr,
						order);
				}
			}
		}

		if( !progressed )
		{
			// We should never have been able to submit/approve
			throw new RuntimeException(resources.getString("order.error.deadendworkflow"));
		}
		order.setStatus(historyItem.getStatus());
		order.setLastActionDate(historyItem.getDate());
		order.setLastActionUser(historyItem.getUserId());

		order.getHistory().add(historyItem);
	}

	/**
	 * throws InvalidDataException (an unchecked exception)
	 */
	@Transactional
	@Override
	public Order rejectOrder(Order order, String comment)
	{
		final List<ValidationError> errors = new ArrayList<ValidationError>();
		if( Strings.isNullOrEmpty(comment) )
		{
			errors.add(new ValidationError(KEY_COMMENT, resources.getString("order.error.mandatory.rejectcomment")));
			throw new InvalidDataException(errors);
		}
		order.setApproversExpression(null);
		order.setPayersExpression(null);
		order.setStatus(Status.REJECTED);

		final OrderHistory oc = new OrderHistory();
		oc.setDate(new Date());
		oc.setUserId(CurrentUser.getUserID());
		oc.setComment(comment);
		oc.setStatus(Status.REJECTED);
		order.getHistory().add(oc);

		dao.save(order);

		return order;
	}

	@Transactional
	@Override
	public Order redraftOrder(Order order)
	{
		final Order currentCart = dao.getShoppingCart();
		if( currentCart != null )
		{
			dao.delete(currentCart);
		}

		order.setStatus(Status.CART);
		order.getHistory().clear();
		order.setApproversExpression(null);
		order.setPayersExpression(null);
		order.setLastActionDate(null);
		order.setLastActionUser(null);

		dao.save(order);

		return order;
	}

	@Transactional
	@Override
	public void deleteOrder(Order order)
	{
		dao.delete(order);
	}

	@Override
	public boolean isEmpty(Order order)
	{
		for( OrderStorePart sp : order.getStoreParts() )
		{
			if( sp.getOrderItems().size() > 0 )
			{
				return false;
			}
		}
		return true;
	}

	@Transactional
	@Override
	public StoreCheckoutBean submitToStore(Order order, Store store) throws OrderValueChangedException
	{
		final StorePricingInformationBean pricingInfo = shopService.getPricingInformation(store, true);
		calculateStoreTotalFinal(order, store, pricingInfo);
		final OrderStorePart storePart = getStorePart(order, store);

		final StoreCheckoutBean checkout = shopService.submitOrder(store, storePart, pricingInfo.getTax());
		storePart.setCheckoutUuid(checkout.getUuid());

		final String userId = CurrentUser.getUserID();
		final Date lastActionDate = new Date();

		// Split out the store part into a new Order
		if( order.getStoreParts().size() > 1 )
		{
			final Order newOrder = new Order();
			newOrder.setOriginalOrderUuid(order.getUuid());
			newOrder.setCreatedBy(order.getCreatedBy());
			newOrder.setCreatedDate(lastActionDate);
			newOrder.setInstitution(CurrentInstitution.get());
			newOrder.setUuid(UUID.randomUUID().toString());
			newOrder.setLastActionDate(lastActionDate);
			newOrder.setLastActionUser(userId);
			newOrder.setStatus(Status.PENDING);
			newOrder.setPayersExpression(order.getPayersExpression());
			newOrder.setApproversExpression(order.getApproversExpression());

			final OrderStorePart newPart = cloneStorePart(storePart);
			newPart.setOrder(newOrder);
			newOrder.getStoreParts().add(newPart);

			// copy the history from old order
			final List<OrderHistory> newHistory = newOrder.getHistory();
			for( OrderHistory hist : order.getHistory() )
			{
				final OrderHistory historyItem = new OrderHistory();
				historyItem.setOrder(newOrder);
				historyItem.setComment(hist.getComment());
				historyItem.setDate(hist.getDate());
				historyItem.setStatus(hist.getStatus());
				historyItem.setUserId(hist.getUserId());
				newHistory.add(historyItem);
			}

			final OrderHistory historyItem = new OrderHistory();
			historyItem.setOrder(newOrder);
			historyItem.setDate(lastActionDate);
			historyItem.setStatus(Status.PENDING);
			historyItem.setUserId(userId);
			newHistory.add(historyItem);

			dao.save(newOrder);

			order.getStoreParts().remove(storePart);
			dao.save(order);
		}
		else
		{
			if( order.getStatus() == Status.CART )
			{
				// Was a shopping cart, but now a proper order, so we'll set the
				// created date
				order.setCreatedDate(new Date());
			}
			order.setStatus(Status.PENDING);
			order.setLastActionDate(lastActionDate);
			order.setLastActionUser(userId);

			final OrderHistory historyItem = new OrderHistory();
			historyItem.setOrder(order);
			historyItem.setDate(lastActionDate);
			historyItem.setStatus(Status.PENDING);
			historyItem.setUserId(userId);
			order.getHistory().add(historyItem);

			dao.save(order);
		}
		notificationService.removeAllForUuid(order.getUuid(),
			Lists.newArrayList(NOTIFICATION_REASON_REQUIRES_PAYMENT, NOTIFICATION_REASON_REQUIRES_APPROVAL));

		return checkout;
	}

	private OrderStorePart cloneStorePart(OrderStorePart storePart)
	{
		final OrderStorePart newPart = new OrderStorePart();
		newPart.setCheckoutUuid(storePart.getCheckoutUuid());
		newPart.setPaid(storePart.getPaid());
		newPart.setPaidDate(storePart.getPaidDate());
		newPart.setPrice(storePart.getPrice());
		newPart.setTax(storePart.getTax());
		newPart.setTaxCode(storePart.getTaxCode());
		newPart.setCurrency(storePart.getCurrency());
		newPart.setUuid(storePart.getUuid());
		newPart.setStore(storePart.getStore());

		for( OrderItem item : storePart.getOrderItems() )
		{
			final OrderItem newItem = new OrderItem();
			newItem.setAddedDate(item.getAddedDate());
			newItem.setCatUuid(item.getCatUuid());
			newItem.setCurrency(item.getCurrency());
			newItem.setItemUuid(item.getItemUuid());
			newItem.setItemVersion(item.getItemVersion());
			newItem.setName(item.getName());
			newItem.setOrderStorePart(newPart);
			newItem.setPeriodUuid(item.getPeriodUuid());
			newItem.setPerUser(item.isPerUser());
			newItem.setPrice(item.getPrice());
			newItem.setTax(item.getTax());
			newItem.setPurchaseTierUuid(item.getPurchaseTierUuid());
			newItem.setSubscriptionTierUuid(item.getSubscriptionTierUuid());
			newItem.setSubscriptionStartDate(item.getSubscriptionStartDate());
			newItem.setUnitPrice(item.getUnitPrice());
			newItem.setUnitTax(item.getUnitTax());
			newItem.setTaxCode(item.getTaxCode());
			newItem.setUsers(item.getUsers());
			newItem.setUuid(item.getUuid());
			newPart.getOrderItems().add(newItem);
		}

		return newPart;
	}

	@Transactional
	@Override
	public Purchase payOrderPart(OrderStorePart storePart, StoreTransactionBean transaction)
	{
		final Purchase purch = purchaseService.createPurchase(storePart, transaction);
		final StoreTransactionBean.PaidStatus paidStatus = transaction.getPaidStatus();

		// It kind of has to be...
		if( paidStatus != null && paidStatus == StoreTransactionBean.PaidStatus.PAID )
		{
			storePart.setPaid(PaidStatus.PAID);
			storePart.setPaidDate(new Date());
		}

		return purch;
	}

	@Override
	public List<Order> enumerateSent()
	{
		return dao.enumerateSent(CurrentUser.getUserID());
	}

	@Override
	public List<Order> enumerateApprovalForUser()
	{
		final List<Order> orders = dao.enumerateApproval();
		final List<Order> approvals = Lists.newArrayList();

		final AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
		final UserState state = CurrentUser.getUserState();
		final ApprovalsPaymentsSettings rules = configService.getProperties(new ApprovalsPaymentsSettings());
		for( Order order : orders )
		{
			final String expression = order.getApproversExpression();
			final boolean owner = isOwner(order, state);
			if( !Check.isEmpty(expression) && evaluator.evaluate(expression, state, owner) )
			{
				// even if they match an approver rule, they can't approve if no
				// payment rule to push to
				final Pair<String, String> rule = getPayerRule(state, rules, Side.LHS, owner);
				if( rule != null )
				{
					approvals.add(order);
				}
			}
		}
		return approvals;
	}

	@Override
	public List<Order> enumeratePaymentForUser()
	{
		// Pending orders might not have been paid for
		final List<Order> orders = dao.enumeratePaymentAndPending();
		final List<Order> payments = Lists.newArrayList();
		final AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
		final UserState state = CurrentUser.getUserState();
		for( Order order : orders )
		{
			final String expression = order.getPayersExpression();
			if( !Check.isEmpty(expression) && evaluator.evaluate(expression, state, isOwner(order, state)) )
			{
				payments.add(order);
			}
		}
		return payments;
	}

	@Override
	public Order get(long id)
	{
		return dao.findById(id);
	}

	@Override
	public Order getByUuid(String uuid)
	{
		return dao.getByUuid(uuid);
	}

	@Override
	public List<Order> getByUuidsWithStatus(List<String> uuids, Order.Status status)
	{
		return dao.getByUuidsWithStatus(uuids, status);
	}

	@Override
	public Map<Currency, CurrencyTotal> calculateCurrencyTotals(Order order)
	{
		final Map<Currency, CurrencyTotal> totes = Maps.newHashMap();
		for( OrderStorePart storePart : order.getStoreParts() )
		{
			for( OrderItem item : storePart.getOrderItems() )
			{
				final long price = item.getPrice();
				if( price > 0L )
				{
					final Currency currency = item.getCurrency();
					CurrencyTotal tote = totes.get(currency);
					if( tote == null )
					{
						tote = new CurrencyTotal(currency);
						totes.put(currency, tote);
					}
					final int decimals = (currency == null ? 0 : currency.getDefaultFractionDigits());
					tote.incrementValue(new BigDecimal(price).movePointLeft(decimals));

					final TaxTotal taxTotal = tote.getTaxTotal(item.getTaxCode());
					if( taxTotal != null )
					{
						taxTotal.incrementValue(new BigDecimal(item.getTax()).movePointLeft(decimals));
					}
				}
			}
		}
		return totes;
	}

	/**
	 * This method may modify OrderItems with correct price values. If it does
	 * it will throw an OrderValueChangedException so you will know about it. Be
	 * prepared to catch it. Use this method just before sending off the order
	 * to be paid.
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
	private StoreTotal calculateStoreTotalFinal(Order order, Store store, StorePricingInformationBean pricing)
		throws OrderValueChangedException
	{
		final StoreTotal totes = calculateStoreTotal(order, store, pricing);
		if( totes.isChanged() )
		{
			throw new OrderValueChangedException();
		}
		return totes;
	}

	@Transactional
	@Override
	public StoreTotal calculateStoreTotal(Order order, Store store, StorePricingInformationBean pricing)
	{
		final StoreTotal totes = new StoreTotal();

		final Map<String, StorePurchaseTierBean> purchaseTierMap = Maps.newHashMap();
		final List<StorePurchaseTierBean> purchaseTiers = pricing.getPurchaseTiers();
		if( purchaseTiers != null )
		{
			for( StorePurchaseTierBean ptb : purchaseTiers )
			{
				purchaseTierMap.put(ptb.getUuid(), ptb);
			}
		}

		// The key is the tierUuid+periodUuid
		final Map<String, StorePriceBean> subscriptionTierAndPeriodMap = Maps.newHashMap();

		final List<StoreSubscriptionTierBean> subscriptionTiers = pricing.getSubscriptionTiers();
		if( subscriptionTiers != null )
		{
			for( StoreSubscriptionTierBean stb : subscriptionTiers )
			{
				for( StorePriceBean spb : stb.getPrices() )
				{
					subscriptionTierAndPeriodMap.put(stb.getUuid() + spb.getPeriod().getUuid(), spb);
				}
			}
		}

		final boolean purchasePerUser = pricing.isPurchasePerUser();
		final boolean subscriptionPerUser = pricing.isSubscriptionPerUser();

		final OrderStorePart storePart = getStorePart(order, store);
		for( OrderItem orderItem : storePart.getOrderItems() )
		{
			final String uuid = orderItem.getUuid();
			final Currency oldCurrency = orderItem.getCurrency();
			final int digits = (oldCurrency == null ? 0 : oldCurrency.getDefaultFractionDigits());
			final BigDecimal oldPrice = new BigDecimal(orderItem.getPrice()).movePointLeft(digits);
			final BigDecimal oldTax = new BigDecimal(orderItem.getTax()).movePointLeft(digits);
			final BigDecimal oldUnitPrice = new BigDecimal(orderItem.getUnitPrice()).movePointLeft(digits);
			final BigDecimal oldUnitTax = new BigDecimal(orderItem.getUnitTax()).movePointLeft(digits);
			final String oldTaxCode = orderItem.getTaxCode();

			final String purchaseTierUuid = orderItem.getPurchaseTierUuid();
			final String subscriptionTierUuid = orderItem.getSubscriptionTierUuid();

			final StorePriceBean price;
			final int users;

			if( purchaseTierUuid != null )
			{
				if( purchasePerUser )
				{
					users = orderItem.getUsers();
				}
				else
				{
					users = 1;
					if( orderItem.getUsers() != 0 )
					{
						totes.addError(uuid,
							new ValidationError("users", resources.getString("order.error.usersinflatrate")));
					}
				}

				final StorePurchaseTierBean purchaseTier = purchaseTierMap.get(purchaseTierUuid);
				if( purchaseTier == null )
				{
					totes.addError(uuid,
						new ValidationError("purchaseTierUuid", resources.getString("order.error.tiernotavailable")));
					price = null;
				}
				else
				{
					price = purchaseTier.getPrice();
				}
			}

			// Subscription
			else if( subscriptionTierUuid != null )
			{
				if( subscriptionPerUser )
				{
					users = orderItem.getUsers();
				}
				else
				{
					users = 1;
					if( orderItem.getUsers() != 0 )
					{
						totes.addError(uuid,
							new ValidationError("users", resources.getString("order.error.usersinflatrate")));
					}
				}

				price = subscriptionTierAndPeriodMap.get(subscriptionTierUuid + orderItem.getPeriodUuid());
				if( price == null )
				{
					totes
						.addError(
							uuid,
							new ValidationError("subscriptionTierUuid", resources
								.getString("order.error.tiernotavailable")));
				}
			}

			// Free
			else
			{
				price = null;
				users = 0;
			}

			final BigDecimal unitPrice;
			final BigDecimal unitTax;
			final BigDecimal totalPrice;
			final BigDecimal totalTax;
			final Currency currency;
			final String taxCode;

			if( price == null )
			{
				unitPrice = BigDecimal.ZERO;
				unitTax = BigDecimal.ZERO;
				totalPrice = BigDecimal.ZERO;
				totalTax = BigDecimal.ZERO;
				currency = null;
				taxCode = null;
			}
			else
			{
				currency = Currency.getInstance(price.getCurrency());

				totes.setCurrency(currency);
				unitPrice = bigDecimal(price.getValue());
				// Important! tax applies to the unit price and is rounded
				// accordingly at that level!
				unitTax = bigDecimal(price.getTaxValue()).setScale(currency.getDefaultFractionDigits(),
					RoundingMode.HALF_UP);

				totalPrice = unitPrice.multiply(new BigDecimal(users));
				totalTax = unitTax.multiply(new BigDecimal(users));
				taxCode = getTaxCode(price);

				totes.incrementValue(totalPrice);
				totes.incrementTaxValue(totalTax);
				totes.addTaxCode(taxCode);
			}

			//@formatter:off
			if( changed(oldPrice, totalPrice) 
				|| changed(oldUnitPrice, unitPrice) 
				|| changed(oldTax, totalTax)
				|| changed(oldUnitTax, unitTax) 
				|| !sameCurrency(oldCurrency, currency)
				|| !Objects.equals(oldTaxCode, taxCode) )
			{
				totes.change();
			}
			//@formatter:on

			final int shift = (currency == null ? 0 : currency.getDefaultFractionDigits());
			orderItem.setPrice(toLong(totalPrice, shift));
			orderItem.setTax(toLong(totalTax, shift));
			orderItem.setUnitPrice(toLong(unitPrice, shift));
			orderItem.setUnitTax(toLong(unitTax, shift));
			orderItem.setCurrency(currency);
			orderItem.setTaxCode(taxCode);
		}
		storePart.setPrice(totes.getLongValue());
		storePart.setTax(totes.getLongTaxValue());
		storePart.setCurrency(totes.getCurrency());
		// A bit dodge
		for( String taxCode : totes.getTaxCodes() )
		{
			storePart.setTaxCode(taxCode);
		}
		if( totes.isChanged() )
		{
			dao.save(order);
		}
		return totes;
	}

	private BigDecimal bigDecimal(DecimalNumberBean num)
	{
		return new BigDecimal(BigInteger.valueOf(num.getValue()), (int) num.getScale());
	}

	private boolean changed(BigDecimal oldNum, BigDecimal newNum)
	{
		if( oldNum != null )
		{
			if( newNum == null )
			{
				return true;
			}
			else
			{
				return oldNum.compareTo(newNum) != 0;
			}
		}
		else
		{
			return newNum != null;
		}
	}

	private long toLong(BigDecimal bd, int shift)
	{
		return bd.movePointRight(shift).longValue();
	}

	private String getTaxCode(StorePriceBean price)
	{
		final List<StoreTaxBean> taxes = (price == null ? null : price.getTaxes());
		if( taxes != null )
		{
			for( StoreTaxBean tax : taxes )
			{
				// Bit dodge, only accounts for one tax, but that's OK for now
				return tax.getCode();
			}
		}
		return null;
	}

	@Override
	public StoreTotal softCalculateStoreTotal(Order order, Store store)
	{
		final StoreTotal totes = new StoreTotal();
		for( OrderItem item : getStorePart(order, store).getOrderItems() )
		{
			final Currency currency = item.getCurrency();
			totes.setCurrency(currency);
			if( totes.isMultipleCurrencies() )
			{
				totes.addError(
					item.getUuid(),
					new ValidationError("currency", CurrentLocale
						.get("com.tle.core.payment.storefront.order.error.mismatchcurrencies")));
			}
			final int digits = (currency == null ? 0 : currency.getDefaultFractionDigits());
			totes.incrementValue(new BigDecimal(item.getPrice()).movePointRight(digits));
			totes.incrementTaxValue(new BigDecimal(item.getTax()).movePointRight(digits));
		}
		return totes;
	}

	@Override
	public boolean isTotallyFree(Order order)
	{
		for( OrderStorePart sp : order.getStoreParts() )
		{
			for( OrderItem oi : sp.getOrderItems() )
			{
				if( oi.getPrice() != 0L )
				{
					return false;
				}
			}
		}
		return true;
	}

	private boolean sameCurrency(Currency c1, Currency c2)
	{
		if( c1 == null && c2 == null )
		{
			return true;
		}
		if( c1 == null )
		{
			return false;
		}
		if( c2 == null )
		{
			return false;
		}
		return c1.equals(c2);
	}

	@Override
	public int getItemCount(Order order)
	{
		int count = 0;
		for( OrderStorePart storePart : order.getStoreParts() )
		{
			count += storePart.getOrderItems().size();
		}
		return count;
	}

	@Override
	public OrderWorkflowInfo getOrderProcess(Order order)
	{
		final OrderWorkflowInfo winfo = new OrderWorkflowInfo();

		final Status orderStatus = order.getStatus();
		if( orderStatus == Status.COMPLETE )
		{
			return winfo;
		}

		winfo.setCart(orderStatus == Status.CART);

		final ApprovalsPaymentsSettings settings = configService.getProperties(new ApprovalsPaymentsSettings());
		if( !settings.isEnabled() )
		{
			winfo.setPay(true);
			return winfo;
		}

		final UserState userState = CurrentUser.getUserState();
		final boolean owner = isOwner(order, userState);
		boolean reject = false;
		boolean submitToPayer = false;
		boolean submitToApprover = false;
		boolean pay = false;
		boolean redraft = (orderStatus == Status.REJECTED && owner);

		final AclExpressionEvaluator evaluator = new AclExpressionEvaluator();

		boolean bypassApproval = false;
		// You can pay a cart or an order awaiting payment, but NOT something
		// submitted for approval or already completed
		boolean checkPaymentRules = (/* orderStatus != Status.PENDING && */orderStatus != Status.COMPLETE && orderStatus != Status.APPROVAL);

		// If cart/order is in a state to forward to approver
		if( orderStatus == Status.CART || orderStatus == Status.REJECTED || orderStatus == Status.APPROVAL )
		{
			// approverExpression is the RHS of an approval rule
			String approverExpression = order.getApproversExpression();
			if( approverExpression == null )
			{
				final Pair<String, String> approverRule = getApproverRule(userState, settings, Side.LHS, owner);
				approverExpression = (approverRule == null ? null : approverRule.getSecond());
			}
			if( Check.isEmpty(approverExpression) )
			{
				// No approval rules means no payment!
				checkPaymentRules = false;
			}
			else
			{
				submitToApprover = true;

				// If you match the approval RHS you can approve
				if( evaluator.evaluate(approverExpression, userState, owner) )
				{
					// If it's *totally* free, an approver can 'pay' for it.
					if( isTotallyFree(order) )
					{
						pay = true;
						checkPaymentRules = false;
					}
					else
					{
						// You can skip approval if there is a payment rule to
						// send it to
						// Note: it only allows you to do this if the order is
						// not in APPROVAL state, otherwise the order appears in
						// your approval list yet shows payment buttons
						if( orderStatus != Status.APPROVAL )
						{
							final Pair<String, String> payerRule = getPayerRule(userState, settings, Side.LHS, owner);
							if( payerRule != null )
							{
								bypassApproval = true;
							}
						}
						else
						{
							submitToPayer = true;
						}
					}
					reject = true;
				}
				else
				{
					checkPaymentRules = false;
				}
			}
		}

		if( checkPaymentRules || bypassApproval )
		{
			// payerExpression is the RHS of a payer rule
			String payerExpression = order.getPayersExpression();
			if( payerExpression == null )
			{
				final Pair<String, String> payerRule = getPayerRule(userState, settings, Side.LHS, owner);
				payerExpression = (payerRule == null ? null : payerRule.getSecond());
			}

			if( !Check.isEmpty(payerExpression) )
			{
				// If you match the payer RHS you can pay
				if( evaluator.evaluate(payerExpression, userState, owner) )
				{
					pay = true;
					reject = true;
				}
				else
				{
					submitToPayer = true;
				}
			}
		}

		// *** Clean data ***

		// No self rejection, no cart rejection
		reject = reject && !owner && (orderStatus != Status.CART && orderStatus != Status.REJECTED);

		// Order has already reached payment. No workflow forward
		if( orderStatus == Status.PAYMENT || orderStatus == Status.PENDING )
		{
			submitToPayer = false;
			submitToApprover = false;
		}

		// If already approval, can't submit to approver again
		if( orderStatus == Status.APPROVAL )
		{
			submitToApprover = false;
		}

		// If you can pay, then no workflow
		if( pay )
		{
			submitToPayer = false;
			submitToApprover = false;
		}

		// If you can submit to payer, no need to submit to approver
		if( submitToPayer )
		{
			submitToApprover = false;
		}

		winfo.setReject(reject);
		winfo.setPay(pay);
		winfo.setSubmitApprove(submitToApprover);
		winfo.setSubmitPay(submitToPayer);
		winfo.setRedraft(redraft);
		return winfo;
	}

	/**
	 * @param order
	 * @param userState
	 * @return
	 */
	private boolean isOwner(Order order, UserState userState)
	{
		return order.getCreatedBy().equals(userState.getUserBean().getUniqueID());
	}

	/**
	 * @param approvals check the approval rules, otherwise the payment rules
	 * @return
	 */
	private Pair<String, String> getApproverRule(UserState userState, ApprovalsPaymentsSettings settings, Side side,
		boolean owner)
	{
		if( !settings.isEnabled() )
		{
			return null;
		}
		return getFirstMatchingRule(userState, settings.getApprovals(), side, owner);
	}

	private Pair<String, String> getPayerRule(UserState userState, ApprovalsPaymentsSettings settings, Side side,
		boolean owner)
	{
		if( !settings.isEnabled() )
		{
			return null;
		}
		return getFirstMatchingRule(userState, settings.getPayments(), side, owner);
	}

	private Pair<String, String> getFirstMatchingRule(UserState userState, List<ApprovalsPayments> rules, Side side,
		boolean owner)
	{
		final AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
		for( ApprovalsPayments a : rules )
		{
			if( evaluator.evaluate(side == Side.LHS ? a.getExpressionFrom() : a.getExpressionTo(), userState, owner) )
			{
				return new Pair<String, String>(a.getExpressionFrom(), a.getExpressionTo());
			}
		}
		return null;
	}

	@Override
	public boolean isCurrentUserAnApprover()
	{
		final ApprovalsPaymentsSettings settings = configService.getProperties(new ApprovalsPaymentsSettings());
		if( !settings.isEnabled() )
		{
			return false;
		}
		final Pair<String, String> approverRule = getApproverRule(CurrentUser.getUserState(), settings, Side.RHS, false);
		return approverRule != null;
	}

	@Override
	public boolean isCurrentUserAPayer()
	{
		final ApprovalsPaymentsSettings settings = configService.getProperties(new ApprovalsPaymentsSettings());
		if( !settings.isEnabled() )
		{
			return false;
		}
		final Pair<String, String> payerRule = getPayerRule(CurrentUser.getUserState(), settings, Side.RHS, false);
		return payerRule != null;
	}

	@Override
	public boolean canView(Order order)
	{
		String userId = CurrentUser.getUserID();
		if( order.getCreatedBy().equals(userId) )
		{
			return true;
		}

		final ApprovalsPaymentsSettings settings = configService.getProperties(new ApprovalsPaymentsSettings());
		if( settings.isEnabled() )
		{
			final UserState userState = CurrentUser.getUserState();
			final boolean owner = isOwner(order, userState);
			final AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
			final Status status = order.getStatus();
			if( status == Status.APPROVAL )
			{
				return evaluator.evaluate(order.getApproversExpression(), userState, owner);
			}

			if( status == Status.PAYMENT || status == Status.PENDING || status == Status.COMPLETE )
			{
				// Payer expression could be null if the cart builder paid it
				// themselves
				String payersExpression = order.getPayersExpression();
				if( payersExpression == null )
				{
					// check if someone *could* have paid it
					final Pair<String, String> payerRule = runAsUser.execute(CurrentInstitution.get(), userId,
						new Callable<Pair<String, String>>()
						{
							@Override
							public Pair<String, String> call()
							{
								return getPayerRule(CurrentUser.getUserState(), settings, Side.LHS, owner);
							}
						});
					if( payerRule != null )
					{
						payersExpression = payerRule.getSecond();
					}
				}
				if( payersExpression != null )
				{
					return evaluator.evaluate(payersExpression, userState, owner);
				}
			}
		}

		return false;
	}

	@Override
	public OrderStorePart getStorePart(Order order, Store store)
	{
		for( OrderStorePart storePart : order.getStoreParts() )
		{
			if( storePart.getStore().getUuid().equals(store.getUuid()) )
			{
				return storePart;
			}
		}
		return null;
	}

	@Override
	public OrderItem getOrderItem(Order order, Store store, String itemUuid)
	{
		OrderStorePart storePart = getStorePart(order, store);
		if( storePart == null )
		{
			return null;
		}
		for( OrderItem orderItem : storePart.getOrderItems() )
		{
			if( orderItem.getItemUuid().equals(itemUuid) )
			{
				return orderItem;
			}
		}
		return null;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Purchase updateIncompleteOrder(String orderUuid)
	{
		Purchase purch = null;
		Order order = dao.getByUuid(orderUuid);
		final List<OrderStorePart> storeParts = order.getStoreParts();
		if( storeParts.size() != 1 )
		{
			throw new RuntimeException(resources.getString("order.error.onestoreperpendingorder"));
		}

		final OrderStorePart part = storeParts.get(0);
		final Store store = part.getStore();
		final String checkoutUuid = part.getCheckoutUuid();
		if( !Check.isEmpty(checkoutUuid) && PaidStatus.PAID != part.getPaid() )
		{
			final StoreTransactionBean transaction = shopService.getTransaction(store, checkoutUuid);

			if( StoreTransactionBean.PaidStatus.PAID == transaction.getPaidStatus() )
			{
				purch = payOrderPart(part, transaction);

				// archive the order if part is paid
				if( part.getPaid().equals(PaidStatus.PAID) )
				{
					final Date date = new Date();
					order.setStatus(Status.COMPLETE);
					order.setLastActionDate(date);
					String userId = CurrentUser.getUserID();
					order.setLastActionUser(userId);

					// Add history item
					final OrderHistory history = new OrderHistory();
					history.setDate(date);
					history.setOrder(order);
					history.setStatus(Status.COMPLETE);
					history.setUserId(userId);
					order.getHistory().add(history);

					dao.save(order);
				}
			}
		}
		return purch;
	}

	@Override
	public void startCheckCurrentOrders(boolean wait)
	{
		purchaseService.startCheckCurrentOrders();
	}

	@Transactional
	@Override
	public void checkCurrentOrders()
	{
		boolean newContent = false;
		final List<String> unfinishedOrderUuids = dao.getUnfinishedOrderUuids();
		for( String uuid : unfinishedOrderUuids )
		{
			try
			{
				Purchase purchase = updateIncompleteOrder(uuid);
				newContent |= (purchase != null && purchase.isPaid());
			}
			catch( Exception t )
			{
				LOGGER.error("Error updating incomplete order", t);
			}
		}

		if( newContent )
		{
			purchaseService.startCheckDownloadableContentAndCheckSubscriptions();
		}
	}

	/**
	 * The approvers expression at its simplest is a user uuid in the form
	 * U:aaaaaaaa-bbbb(etc), where the initial capital denotes 'user' rather
	 * than G = group. A more complex expression is a compound of users, groups
	 * and the boolean operator (presumably 'OR') Ignoring any operands, we
	 * separate users and group uuids, and from any group uuids present, search
	 * for the users and add to a set of user uuids.
	 * <p>
	 * From a the set of user uuids, evaluate the expression to see if each user
	 * is a match. If they don't match then remove them. Now we have a final
	 * list of user ids which we use to obtain the email addresses (if the user
	 * has one), and send an email to each. The orderer's comment if any, is
	 * included in the body of the email.
	 * 
	 * @param approverOrPayerExpression
	 * @param userState
	 * @param order
	 */
	private void notifyApproversOrPayers(String notificationReason, final String approverOrPayerExpression,
		final Order order)
	{
		final String[] tokens = approverOrPayerExpression.split("\\s+");

		// Parse out all possible users involved in the expression
		final Set<String> userUuids = Sets.newHashSet();
		for( String token : tokens )
		{
			try
			{
				String value = getRecipientValue(token);
				switch( getRecipientType(token) )
				{
					case USER:
						userUuids.add(value);
						break;
					case GROUP:
						userUuids.addAll(groupDao.getUsersInGroup(value, true));
						break;
					default:
						break;
				}
			}
			catch( IllegalArgumentException iae )
			{
				// An AND or OR or the like
			}
		}

		// Evaluate the expression for each possible user
		final AclExpressionEvaluator eval = new AclExpressionEvaluator();
		final Iterator<String> it = userUuids.iterator();
		while( it.hasNext() )
		{
			runAsUser.execute(CurrentInstitution.get(), it.next(), new Runnable()
			{
				@Override
				public void run()
				{
					final UserState userState = CurrentUser.getUserState();
					if( !eval.evaluate(approverOrPayerExpression, userState, isOwner(order, userState)) )
					{
						it.remove();
					}
				}
			});
		}

		// Clear any old notifications
		notificationService.removeAllForUuid(order.getUuid(),
			Lists.newArrayList(NOTIFICATION_REASON_REQUIRES_PAYMENT, NOTIFICATION_REASON_REQUIRES_APPROVAL));
		if( !Check.isEmpty(userUuids) )
		{
			notificationService.addNotifications(order.getUuid(), notificationReason, userUuids, true);
		}
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		dao.deleteAllForCreator(event.getUserID());
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Don't care
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		final String fromUserId = event.getFromUserId();
		final String toUserId = event.getToUserId();

		dao.updateCreator(fromUserId, toUserId);
		dao.updateLastActionUser(fromUserId, toUserId);
		final List<OrderHistory> history = dao.findHistoryForUser(fromUserId);
		for( OrderHistory hist : history )
		{
			hist.setUserId(toUserId);
		}
	}

	@Override
	public void execute()
	{
		startCheckCurrentOrders(false);
	}
}
