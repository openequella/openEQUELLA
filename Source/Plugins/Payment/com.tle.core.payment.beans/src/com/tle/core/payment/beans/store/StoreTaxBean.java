package com.tle.core.payment.beans.store;

/**
 * @author Aaron
 */
public class StoreTaxBean
{
	private DecimalNumberBean rate;
	private String code;

	public DecimalNumberBean getRate()
	{
		return rate;
	}

	public void setRate(DecimalNumberBean rate)
	{
		this.rate = rate;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}
}
