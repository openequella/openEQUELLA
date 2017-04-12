package com.tle.core.payment.beans.store;

import java.util.List;

/**
 * @author Aaron
 */
public class StorePriceBean
{
	private StoreSubscriptionPeriodBean period; // won't be set for purchase
												// prices
	private DecimalNumberBean value; // cents (well, value without decimals
										// anyway)
	private DecimalNumberBean taxValue;
	private List<StoreTaxBean> taxes;
	private String currency;

	public StoreSubscriptionPeriodBean getPeriod()
	{
		return period;
	}

	public void setPeriod(StoreSubscriptionPeriodBean period)
	{
		this.period = period;
	}

	public DecimalNumberBean getValue()
	{
		return value;
	}

	public void setValue(DecimalNumberBean value)
	{
		this.value = value;
	}

	public DecimalNumberBean getTaxValue()
	{
		return taxValue;
	}

	public void setTaxValue(DecimalNumberBean taxValue)
	{
		this.taxValue = taxValue;
	}

	public List<StoreTaxBean> getTaxes()
	{
		return taxes;
	}

	public void setTaxes(List<StoreTaxBean> taxes)
	{
		this.taxes = taxes;
	}

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}
}
