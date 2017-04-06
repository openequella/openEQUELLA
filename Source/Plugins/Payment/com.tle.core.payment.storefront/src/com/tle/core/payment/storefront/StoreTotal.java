package com.tle.core.payment.storefront;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.common.valuebean.ValidationError;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Aaron
 */
public class StoreTotal
{
	private boolean changed;
	private Currency currency;
	private boolean multipleCurrencies;
	private BigDecimal value = BigDecimal.ZERO;
	private BigDecimal taxValue = BigDecimal.ZERO;
	private final Set<String> taxCodes = Sets.newHashSet();
	private final Map<String, ValidationError> orderItemErrors = Maps.newHashMap();

	public boolean isChanged()
	{
		return changed;
	}

	public void change()
	{
		this.changed = true;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public void setCurrency(Currency currency)
	{
		if( currency != null )
		{
			if( this.currency != null && !this.currency.equals(currency) )
			{
				multipleCurrencies = true;
				this.currency = null;
			}
			else
			{
				this.currency = currency;
			}
		}
	}

	/**
	 * In real life, this would never be true. The store would not be changing
	 * the currencies.
	 * 
	 * @return
	 */
	public boolean isMultipleCurrencies()
	{
		return multipleCurrencies;
	}

	public BigDecimal getValue()
	{
		return value;
	}

	public long getLongValue()
	{
		int digits = (currency == null ? 0 : currency.getDefaultFractionDigits());
		return value.movePointRight(digits).longValue();
	}

	public BigDecimal getTaxValue()
	{
		return taxValue;
	}

	public long getLongTaxValue()
	{
		int digits = (currency == null ? 0 : currency.getDefaultFractionDigits());
		return taxValue.movePointRight(digits).longValue();
	}

	public void incrementValue(BigDecimal modPrice)
	{
		value = value.add(modPrice);
	}

	public void incrementTaxValue(BigDecimal modTax)
	{
		taxValue = taxValue.add(modTax);
	}

	public Set<String> getTaxCodes()
	{
		return taxCodes;
	}

	public void addTaxCode(String taxCode)
	{
		if( taxCode != null )
		{
			taxCodes.add(taxCode);
		}
	}

	/**
	 * Map of OrderItem UUIDs to ValidationErrors
	 * 
	 * @return
	 */
	public Map<String, ValidationError> getErrors()
	{
		return orderItemErrors;
	}

	public boolean isErrored()
	{
		return !orderItemErrors.isEmpty();
	}

	public void addError(String orderItemUuid, ValidationError error)
	{
		if( !orderItemErrors.containsKey(orderItemUuid) )
		{
			orderItemErrors.put(orderItemUuid, error);
		}
	}
}
