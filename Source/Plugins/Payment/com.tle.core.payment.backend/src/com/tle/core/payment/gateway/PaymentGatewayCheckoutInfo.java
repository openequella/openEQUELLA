package com.tle.core.payment.gateway;

/**
 * @author Aaron
 */
public class PaymentGatewayCheckoutInfo
{
	private String buttonImageUrl;
	private String checkoutUrl;

	public String getButtonImageUrl()
	{
		return buttonImageUrl;
	}

	public void setButtonImageUrl(String buttonImageUrl)
	{
		this.buttonImageUrl = buttonImageUrl;
	}

	public String getCheckoutUrl()
	{
		return checkoutUrl;
	}

	public void setCheckoutUrl(String checkoutUrl)
	{
		this.checkoutUrl = checkoutUrl;
	}
}
