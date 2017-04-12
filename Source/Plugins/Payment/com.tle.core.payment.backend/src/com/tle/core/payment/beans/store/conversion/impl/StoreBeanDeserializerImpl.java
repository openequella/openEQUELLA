package com.tle.core.payment.beans.store.conversion.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.collect.Lists;
import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SaleItem;
import com.tle.common.payment.entity.StoreFront;
import com.tle.core.guice.Bind;
import com.tle.core.payment.beans.store.DecimalNumberBean;
import com.tle.core.payment.beans.store.StoreCheckoutBean;
import com.tle.core.payment.beans.store.StoreCheckoutItemBean;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.beans.store.StorePurchaseTierBean;
import com.tle.core.payment.beans.store.StoreSubscriptionPeriodBean;
import com.tle.core.payment.beans.store.StoreSubscriptionTierBean;
import com.tle.core.payment.beans.store.StoreTaxBean;
import com.tle.core.payment.beans.store.conversion.StoreBeanDeserializer;
import com.tle.core.payment.service.PaymentService;
import com.tle.core.user.CurrentInstitution;

/**
 * @author Aaron
 */
@Bind(StoreBeanDeserializer.class)
@Singleton
public class StoreBeanDeserializerImpl implements StoreBeanDeserializer
{
	@Inject
	private PaymentService paymentService;

	@Override
	public Sale convertCartBeanToSale(StoreFront storeFront, StoreCheckoutBean cart)
	{
		final Sale sale = new Sale();
		sale.setStorefront(storeFront);
		sale.setInstitution(CurrentInstitution.get());

		final StorePriceBean price = cart.getPrice();
		if( price != null )
		{
			final String cartCurrency = price.getCurrency();
			if( cartCurrency != null )
			{
				sale.setCurrency(Currency.getInstance(cartCurrency));
			}
			sale.setPrice(price.getValue().getValue());
			DecimalNumberBean taxValue = price.getTaxValue();
			sale.setTax(taxValue == null ? 0 : taxValue.getValue());

			final List<StoreTaxBean> taxes = price.getTaxes();
			if( taxes != null )
			{
				// A bit dodge
				for( StoreTaxBean taxBean : taxes )
				{
					sale.setTaxCode(taxBean.getCode());
					final DecimalNumberBean rate = taxBean.getRate();
					if( rate != null )
					{
						sale.setTaxPercent(new BigDecimal(BigInteger.valueOf(rate.getValue()), (int) rate.getScale()));
					}
				}
			}
		}

		sale.setUuid(cart.getUuid());
		sale.setCustomerReference(cart.getCustomerReference());
		final List<SaleItem> saleItems = Lists.newArrayList();
		final List<StoreCheckoutItemBean> items = cart.getItems();
		if( items != null )
		{
			for( StoreCheckoutItemBean item : items )
			{
				saleItems.add(convertCartItemBeanToSaleItem(item, sale));
			}
		}
		sale.setSales(saleItems);
		return sale;
	}

	@Override
	public SaleItem convertCartItemBeanToSaleItem(StoreCheckoutItemBean cartItem, Sale sale)
	{
		final SaleItem saleItem = new SaleItem();
		saleItem.setSale(sale);

		final StorePurchaseTierBean purchaseTier = cartItem.getPurchaseTier();
		if( purchaseTier != null )
		{
			saleItem.setPricingTierUuid(purchaseTier.getUuid());
		}
		final StoreSubscriptionTierBean subscriptionTier = cartItem.getSubscriptionTier();
		if( subscriptionTier != null )
		{
			saleItem.setPricingTierUuid(subscriptionTier.getUuid());
		}
		saleItem.setItemUuid(cartItem.getItemUuid());
		saleItem.setItemVersion(cartItem.getItemVersion());
		final StorePriceBean price = cartItem.getPrice();
		if( price != null )
		{
			saleItem.setPrice(price.getValue().getValue());
			DecimalNumberBean taxValue = price.getTaxValue();
			saleItem.setTax(taxValue == null ? 0 : taxValue.getValue());
			final List<StoreTaxBean> taxes = price.getTaxes();
			if( taxes != null )
			{
				// A bit dodge
				for( StoreTaxBean taxBean : taxes )
				{
					saleItem.setTaxCode(taxBean.getCode());
				}
			}
		}
		final StorePriceBean unitPrice = cartItem.getUnitPrice();
		if( unitPrice != null )
		{
			saleItem.setUnitPrice(unitPrice.getValue().getValue());
			saleItem.setUnitTax(unitPrice.getTaxValue().getValue());
		}
		saleItem.setQuantity(cartItem.getQuantity());
		saleItem.setUuid(cartItem.getUuid());
		saleItem.setCatalogueUuid(cartItem.getCatalogueUuid());

		final StoreSubscriptionPeriodBean period = cartItem.getSubscriptionPeriod();
		if( period != null && period.getUuid() != null )
		{
			saleItem.setPeriod(paymentService.getSubscriptionPeriodByUuid(period.getUuid()));
			Date startDate = cartItem.getSubscriptionStartDate();
			if( startDate != null )
			{
				startDate = DateUtils.truncate(startDate, Calendar.DATE);
				Date endDate = paymentService.getEndDateOfSubscriptionPeriod(startDate, saleItem.getPeriod());
				saleItem.setSubscriptionStartDate(startDate);
				saleItem.setSubscriptionEndDate(endDate);
			}
		}

		return saleItem;
	}
}
