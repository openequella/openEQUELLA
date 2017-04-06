package com.tle.web.payment.shop.notifications;

import static com.tle.core.payment.storefront.constants.StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_APPROVAL;
import static com.tle.core.payment.storefront.constants.StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_PAYMENT;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.UserBean;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.common.payment.storefront.entity.Order;
import com.tle.common.payment.storefront.entity.OrderStorePart;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.payment.storefront.constants.StoreFrontConstants;
import com.tle.core.payment.storefront.service.OrderService;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.workflow.notification.ItemNotification;
import com.tle.web.workflow.notification.StandardNotifications;

/**
 * This class can appear more than one as a connection point, and in each case
 * it will be keyed to either a type of 'apprvodr' or 'payorder' The resultant
 * email formats are essentially the same, the differences being minor
 * customisations in wording, and (from the callers perspective) to whom the
 * emails are sent.<br>
 * For an example of a similar class which has a multiple of 'type' (ie, tasks)
 * in its typeMap see TaskNotifications
 * 
 * @see com.tle.web.workflow.notification.TaskNotifications
 * @author larry
 */
@Bind
@Singleton
public class ApprovalOrPaymentNotifications extends StandardNotifications
{
	@PlugKey("email.approvalandpayment.header.reason.apprvodr")
	private static Label LABEL_HEADER_APPRVODR;
	@PlugKey("email.approvalandpayment.header.reason.payorder")
	private static Label LABEL_HEADER_PAYORDER;
	@PlugKey("email.approvalandpayment.numberofitems.multiplecurrencies")
	private static Label LABEL_MULTI_CURRENCY_SUMMARY;
	@PlugKey("email.approvalandpayment.noprice")
	private static Label LABEL_NO_PRICE;
	@PlugKey("email.approvalandpayment.userformat")
	private static String KEY_USER;

	@Inject
	private OrderService orderService;
	@Inject
	private UserService userService;
	@Inject
	private ExtendedFreemarkerFactory viewFactory;

	@SuppressWarnings("nls")
	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		final StringWriter writer = new StringWriter();
		// So far only two choices, approval or payment, so a boolean will do.
		// We assume payment in the absence of approval.
		final boolean hasApproval = typeMap.keySet().contains(NOTIFICATION_REASON_REQUIRES_APPROVAL);
		final boolean hasPayment = typeMap.keySet().contains(NOTIFICATION_REASON_REQUIRES_PAYMENT);

		if( hasApproval )
		{
			final EmailOrderNotifications model = createModel(NOTIFICATION_REASON_REQUIRES_APPROVAL, typeMap);
			model.setHeader(LABEL_HEADER_APPRVODR);
			viewFactory.render(viewFactory.createResultWithModel("notification-apprvodr-payorder.ftl", model), writer);
		}

		if( hasPayment )
		{
			final EmailOrderNotifications model = createModel(NOTIFICATION_REASON_REQUIRES_PAYMENT, typeMap);
			model.setHeader(LABEL_HEADER_PAYORDER);
			viewFactory.render(viewFactory.createResultWithModel("notification-apprvodr-payorder.ftl", model), writer);
		}

		return writer.toString();
	}

	private EmailOrderNotifications createModel(String notificationType, ListMultimap<String, Notification> typeMap)
	{
		final Pair<List<Label>, List<OrderNotificationBean>> summaryAndNotifications = createNotifications(
			typeMap.get(notificationType), notificationType);
		final List<OrderNotificationBean> byUserNotifications = summaryAndNotifications.getSecond();

		final EmailOrderNotifications model = new EmailOrderNotifications();
		model.setByUserNotifications(byUserNotifications);
		model.setTaskName(notificationType);
		model.setCurrencySummary(summaryAndNotifications.getFirst());

		return model;
	}

	/**
	 * From a given list of Notifications, extract all the order uuids (stores
	 * as 'itemIds'), and make a single call to the orderService to get a return
	 * set. It's possible for the same order to be the subject of multiple
	 * notifications for the same reason (eg, an order is submitted, rejected,
	 * resubmitted) but duplicates in the orderUuids list won't have any effect.
	 * NB: return set limited to OrderDao.MAX_CURSORY_ENQUIRY (100). From the
	 * returned orders, group by user (as some users may be keen shoppers) and
	 * for each user, compose a single OrderNotificationBean object.
	 * 
	 * @param notifications
	 * @return A List of currency totals paired with a List of
	 *         OrderNotificationBean
	 */
	protected Pair<List<Label>, List<OrderNotificationBean>> createNotifications(List<Notification> notifications,
		String notificationType)
	{
		List<String> orderUuids = new ArrayList<String>(notifications.size());
		for( Notification notification : notifications )
		{
			orderUuids.add(getOrderIdentifier(notification));
		}

		Order.Status orderStatus = notificationType.equals(StoreFrontConstants.NOTIFICATION_REASON_REQUIRES_APPROVAL)
			? Order.Status.APPROVAL : Order.Status.PAYMENT;
		List<Order> notifiableOrders = orderService.getByUuidsWithStatus(orderUuids, orderStatus);

		ListMultimap<String, Order> matchedOrders = ArrayListMultimap.create();
		for( Order order : notifiableOrders )
		{
			matchedOrders.put(order.getCreatedBy(), order);
		}

		List<OrderNotificationBean> byUserNotifications = new ArrayList<OrderNotificationBean>();

		// we've collected orders grouped by distinct users. For each user we
		// now create a OrderNotificationBean. The final return is a list of one
		// Bean per user
		Map<Currency, Long> mappedTotals = new HashMap<Currency, Long>();
		for( String distinctUserUuid : matchedOrders.keySet() )
		{
			OrderNotificationBean beansPerUser = buildByDistinctUser(distinctUserUuid,
				matchedOrders.get(distinctUserUuid), mappedTotals);
			byUserNotifications.add(beansPerUser);
		}
		Collections.sort(byUserNotifications, new Comparator<OrderNotificationBean>()
		{
			@Override
			public int compare(OrderNotificationBean o1, OrderNotificationBean o2)
			{
				return o1.getNameLabel().getText().compareToIgnoreCase(o2.getNameLabel().getText());
			}
		});

		List<Label> totals = Lists.newArrayList();
		for( Entry<Currency, Long> entry : mappedTotals.entrySet() )
		{
			totals.add(new MoneyLabel(entry.getValue(), entry.getKey(), true));
		}
		return new Pair<List<Label>, List<OrderNotificationBean>>(totals, byUserNotifications);
	}

	/**
	 * With a userUuid and a list of orders submitted by that user, compose a
	 * OrderNotificationBean, which will include the user's username, first name
	 * and last name, number of orders and a short summary of the orders
	 * composition. Where all orders share the same currency (hopefully the
	 * common case), we compose a message to the effect:<br>
	 * {0} items, for a total price of {1}(currency: {2}).<br>
	 * Where multiple currencies are found we simply summarise as<br>
	 * {0} items (multiple currencies)
	 * 
	 * @param userUuid
	 * @param userOrders
	 * @return
	 */
	private OrderNotificationBean buildByDistinctUser(String userUuid, List<Order> userOrders,
		Map<Currency, Long> mappedTotals)
	{
		OrderNotificationBean reqApprv = new OrderNotificationBean();
		UserBean userBean = userService.getInformationForUser(userUuid);
		reqApprv.setNameLabel(new CachedKeyLabel(KEY_USER, userBean.getLastName(), userBean.getFirstName()));

		Set<Currency> currencies = new HashSet<Currency>();
		int totalItems = 0;
		// totalPrice is only meaningful where all orders by a single user are
		// in the same currency, otherwise it's ignored
		long totalPrice = 0;
		for( Order order : userOrders )
		{
			for( OrderStorePart orderPart : order.getStoreParts() )
			{
				totalItems += orderPart.getOrderItems().size();
				Currency thisCurrency = orderPart.getCurrency();
				currencies.add(thisCurrency);
				totalPrice += orderPart.getPrice();
				// First orderPart with this currency we've seen?
				if( mappedTotals.get(thisCurrency) == null )
				{
					mappedTotals.put(thisCurrency, orderPart.getPrice());
				}
				else
				// accumulate
				{
					long valueThisCurrencySoFar = mappedTotals.get(thisCurrency);
					mappedTotals.put(thisCurrency, valueThisCurrencySoFar + orderPart.getPrice());
				}
			}
		}
		reqApprv.setItemName(new TextLabel(userBean.getUsername()));
		reqApprv.setNumberOfOrders(userOrders.size());
		reqApprv.setNumberOfItems(totalItems);
		if( currencies.size() > 1 )
		{
			reqApprv.setSummaryOfOrders(LABEL_MULTI_CURRENCY_SUMMARY);
		}
		else
		{
			// So long as we have both a price and a currency, we can construct
			// a styled money label (eg $44.95), otherwise the article has no
			// price, ie is presumably free.
			Currency currency = currencies.iterator().next();
			reqApprv.setSummaryOfOrders(getPriceLabel(totalPrice, currency));
		}
		return reqApprv;
	}

	private Label getPriceLabel(long value, Currency currency)
	{
		if( value == 0L )
		{
			return LABEL_NO_PRICE;
		}
		return new MoneyLabel(value, currency, true);
	}

	private static class CachedKeyLabel extends KeyLabel
	{
		private String text;

		CachedKeyLabel(String key, Object... params)
		{
			super(key, params);
			setHtml(false);
		}

		@Override
		public String getText()
		{
			if( text != null )
			{
				return text;
			}
			text = super.getText();
			return text;
		}
	}

	private String getOrderIdentifier(Notification notification)
	{
		String identifier = notification.getItemidOnly();
		if( identifier.indexOf('/') > 0 )
		{
			identifier = identifier.substring(0, identifier.indexOf('/'));
		}
		return identifier;
	}

	/**
	 * unimplemented: return type (a list of ItemNotification) not appropriate
	 * for this class. This should not be called at all, but at least we can
	 * avoid a breakage if some caller makes an over-wide sweep.
	 */
	@Override
	public List<ItemNotification> createItemNotifications(List<Notification> notifications)
	{
		return new ArrayList<ItemNotification>();
	}

	@Override
	public boolean isIndexed(String type)
	{
		return false;
	}

	@Override
	public boolean isForceEmail(String type)
	{
		return true;
	}

	public static class OrderNotificationBean extends ItemNotification
	{
		private Label nameLabel;
		private int numberOfOrders;
		private int numberOfItems;
		private Label summaryOfOrders;

		public Label getNameLabel()
		{
			return nameLabel;
		}

		public void setNameLabel(Label nameLabel)
		{
			this.nameLabel = nameLabel;
		}

		public int getNumberOfOrders()
		{
			return numberOfOrders;
		}

		public void setNumberOfOrders(int numberOfOrders)
		{
			this.numberOfOrders = numberOfOrders;
		}

		public int getNumberOfItems()
		{
			return numberOfItems;
		}

		public void setNumberOfItems(int numberOfItems)
		{
			this.numberOfItems = numberOfItems;
		}

		public Label getSummaryOfOrders()
		{
			return summaryOfOrders;
		}

		public void setSummaryOfOrders(Label summaryOfOrders)
		{
			this.summaryOfOrders = summaryOfOrders;
		}
	}

	public static class EmailOrderNotifications
	{
		private Label header;
		private Label emptyLabel;
		private String taskName;
		private List<OrderNotificationBean> byUserNotifications;
		private List<Label> currencySummary;

		public Label getHeader()
		{
			return header;
		}

		public void setHeader(Label header)
		{
			this.header = header;
		}

		public Label getEmptyLabel()
		{
			return emptyLabel;
		}

		public void setEmptyLabel(Label emptyLabel)
		{
			this.emptyLabel = emptyLabel;
		}

		public String getTaskName()
		{
			return taskName;
		}

		public void setTaskName(String taskName)
		{
			this.taskName = taskName;
		}

		public List<OrderNotificationBean> getByUserNotifications()
		{
			return byUserNotifications;
		}

		public void setByUserNotifications(List<OrderNotificationBean> byUserNotifications)
		{
			this.byUserNotifications = byUserNotifications;
		}

		public List<Label> getCurrencySummary()
		{
			return currencySummary;
		}

		public void setCurrencySummary(List<Label> currencySummary)
		{
			this.currencySummary = currencySummary;
		}
	}
}
