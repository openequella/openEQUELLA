package com.tle.web.payment.notification;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.payment.PaymentConstants;
import com.tle.core.payment.service.SaleService;
import com.tle.web.TextBundle;
import com.tle.web.i18n.BundleCache;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.MoneyLabel;
import com.tle.web.workflow.notification.ItemNotification;
import com.tle.web.workflow.notification.StandardNotifications;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ItemSaleNotifications extends StandardNotifications
{
	@PlugKey("email.itemsale.header")
	private static Label LABEL_HEADER;

	@PlugKey("email.itemsale.nothingtoreport")
	private static Label LABEL_NOTHING_TO_REPORT;
	@PlugKey("notereason.itemsale.filter")
	private static Label LABEL_REASON_FILTER;
	@PlugKey("notificationlist.itemsale.reason")
	private static Label LABEL_REASON;

	@PlugKey("email.itemsale.type.free")
	private static Label LABEL_FREE;
	@PlugKey("email.itemsale.type.purchase")
	private static Label LABEL_PURCHASE;
	@PlugKey("email.itemsale.type.subscription")
	private static String KEY_SUBSCRIPTION;

	@Inject
	private SaleService saleService;
	@Inject
	private ExtendedFreemarkerFactory viewFactory;
	@Inject
	private BundleCache bundleCache;

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		final List<ItemNotification> itemNotifications = createItemNotifications(typeMap
			.get(PaymentConstants.NOTIFICATION_REASON_ITEMSALE));

		final ItemSaleEmailNotifications model = new ItemSaleEmailNotifications();
		model.setHeader(LABEL_HEADER);
		model.setNotifications(itemNotifications);
		if( itemNotifications.isEmpty() )
		{
			model.setEmptyLabel(LABEL_NOTHING_TO_REPORT);
		}
		else
		{
			model.setCurrencySummary(composeCurrencySummary(itemNotifications));
		}

		final StringWriter writer = new StringWriter();
		viewFactory.render(viewFactory.createResultWithModel("notification-itemsale.ftl", model), writer);
		return writer.toString();
	}

	@Override
	protected ItemNotification createItemNotification(Item item)
	{
		ItemSaleNotification itemSaleNotification = new ItemSaleNotification();
		List<SaleItem> salesItemsForSourceItem = saleService.getSalesItemsForSourceItem(item.getUuid());
		List<SaleItemDisplay> saleItemDisplays = Lists.newArrayList();

		Collections.sort(salesItemsForSourceItem, new NumberStringComparator<SaleItem>()
		{
			@Override
			public String convertToString(SaleItem t)
			{
				return TextBundle.getLocalString(t.getSale().getStorefront().getName(), bundleCache, null, "");
			}
		});

		for( SaleItem saleItem : salesItemsForSourceItem )
		{
			final SaleItemDisplay saleItemDisplay = new SaleItemDisplay();
			final String pricingTierUuid = saleItem.getPricingTierUuid();
			final Sale sale = saleItem.getSale();
			if( pricingTierUuid == null )
			{
				saleItemDisplay.setPrice(LABEL_FREE);
				saleItemDisplay.setType(LABEL_FREE);
			}
			else
			{
				long rawSalePrice = saleItem.getPrice();
				Currency thisCurrency = sale.getCurrency();

				saleItemDisplay.setPrice(getPriceLabel(rawSalePrice, thisCurrency));
				SubscriptionPeriod period = saleItem.getPeriod();
				if( period == null )
				{
					saleItemDisplay.setType(LABEL_PURCHASE);
				}
				else
				{
					saleItemDisplay.setType(new KeyLabel(KEY_SUBSCRIPTION, new BundleLabel(period.getName(),
						bundleCache)));
				}

				if( itemSaleNotification.getMappedCurrencies().get(thisCurrency) != null )
				{
					rawSalePrice += itemSaleNotification.getMappedCurrencies().get(thisCurrency);
				}

				itemSaleNotification.getMappedCurrencies().put(thisCurrency, rawSalePrice);
			}
			saleItemDisplay.setFront(new BundleLabel(sale.getStorefront().getName(), bundleCache));
			saleItemDisplays.add(saleItemDisplay);
		}

		itemSaleNotification.setSaleItems(saleItemDisplays);
		return itemSaleNotification;
	}

	private Label getPriceLabel(long value, Currency currency)
	{
		if( value == 0L )
		{
			return LABEL_FREE;
		}
		return new MoneyLabel(value, currency, true);
	}

	/**
	 * For inheritance reasons, the type of the list's elements are
	 * ItemNotification, but we know that they really are ItemSaleNotification.
	 * 
	 * @param itemNotifications
	 * @return
	 */
	private List<Label> composeCurrencySummary(List<ItemNotification> itemNotifications)
	{
		Map<Currency, Long> superMap = new HashMap<Currency, Long>();
		for( ItemNotification notification : itemNotifications )
		{
			ItemSaleNotification itemSaleNotification = (ItemSaleNotification) notification;
			Map<Currency, Long> submap = itemSaleNotification.getMappedCurrencies();
			for( Entry<Currency, Long> entry : submap.entrySet() )
			{
				Long rawPrice = entry.getValue();
				Long superPrice = superMap.get(entry.getKey());
				if( superPrice != null )
				{
					rawPrice += superPrice;
				}
				superMap.put(entry.getKey(), rawPrice);
			}
		}

		List<Label> currencySummary = Lists.newArrayList();
		for( Entry<Currency, Long> entry : superMap.entrySet() )
		{
			currencySummary.add(new MoneyLabel(entry.getValue(), entry.getKey(), true));
		}

		return currencySummary;
	}

	@Override
	public Label getReasonLabel(String type)
	{
		return LABEL_REASON;
	}

	@Override
	public Label getReasonFilterLabel(String type)
	{
		return LABEL_REASON_FILTER;
	}

	public static class ItemSaleEmailNotifications extends EmailNotifications
	{
		private Label emptyLabel;
		private List<Label> currencySummary;

		public Label getEmptyLabel()
		{
			return emptyLabel;
		}

		public void setEmptyLabel(Label emptyLabel)
		{
			this.emptyLabel = emptyLabel;
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

	public static class ItemSaleNotification extends ItemNotification
	{
		private Collection<SaleItemDisplay> saleItems;
		private Map<Currency, Long> mappedCurrencies = new HashMap<Currency, Long>();

		public Collection<SaleItemDisplay> getSaleItems()
		{
			return saleItems;
		}

		public void setSaleItems(Collection<SaleItemDisplay> saleItems)
		{
			this.saleItems = saleItems;
		}

		public Map<Currency, Long> getMappedCurrencies()
		{
			return mappedCurrencies;
		}
	}

	public static class SaleItemDisplay
	{
		private Label front;
		private Label type;
		private Label price;

		public Label getFront()
		{
			return front;
		}

		public void setFront(Label front)
		{
			this.front = front;
		}

		public Label getType()
		{
			return type;
		}

		public void setType(Label type)
		{
			this.type = type;
		}

		public Label getPrice()
		{
			return price;
		}

		public void setPrice(Label price)
		{
			this.price = price;
		}
	}
}
