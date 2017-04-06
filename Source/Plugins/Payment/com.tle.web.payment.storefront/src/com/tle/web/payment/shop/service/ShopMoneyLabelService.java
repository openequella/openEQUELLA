package com.tle.web.payment.shop.service;

import java.math.BigDecimal;
import java.util.Currency;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.payment.beans.store.StorePriceBean;
import com.tle.core.payment.storefront.settings.StoreFrontSettings;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.MoneyLabel;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ShopMoneyLabelService
{
	@PlugKey("shop.money.label.invalid")
	private static Label LABEL_INVALID;
	@PlugKey("shop.money.label.free")
	private static Label LABEL_FREE;

	@Inject
	private ConfigurationService configService;

	public boolean isShowTax()
	{
		StoreFrontSettings settings = configService.getProperties(new StoreFrontSettings());
		return settings.isIncludeTax();
	}

	public Label getLabel(StorePriceBean price, long qty)
	{
		return getLabel(price, qty, isShowTax(), false);
	}

	public Label getLabel(StorePriceBean price, long qty, boolean showTax, boolean zeroInvalid)
	{
		if( price == null )
		{
			if( zeroInvalid )
			{
				return LABEL_INVALID;
			}
			else
			{
				return LABEL_FREE;
			}
		}
		return getLabel(price.getValue().getValue(), price.getTaxValue().getValue(), qty,
			Currency.getInstance(price.getCurrency()), showTax, zeroInvalid);
	}

	public Label getLabel(long unitPrice, long tax, Currency currency)
	{
		return getLabel(unitPrice, tax, 1L, currency, isShowTax(), false);
	}

	public Label getLabel(BigDecimal unitPrice, BigDecimal tax, Currency currency)
	{
		final BigDecimal totes;
		if( tax != null )
		{
			totes = unitPrice.add(isShowTax() ? tax : BigDecimal.ZERO);
		}
		else
		{
			totes = unitPrice;
		}
		if( totes.compareTo(BigDecimal.ZERO) == 0 )
		{
			return LABEL_FREE;
		}
		return new MoneyLabel(totes, currency);
	}

	public Label getLabel(long unitPrice, long tax, long qty, Currency currency, boolean showTax, boolean zeroInvalid)
	{
		final long tax2 = (showTax ? tax : 0L);
		final long totes = (unitPrice + tax2) * qty;
		if( totes == 0L )
		{
			return zeroInvalid ? LABEL_INVALID : LABEL_FREE;
		}
		return new MoneyLabel(totes, currency);
	}
}
