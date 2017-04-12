package com.tle.core.payment.beans.store;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class StorePaymentGatewayBean extends BaseEntityBean
{
	private String checkoutUrl;
	private String buttonUrl;
	private String gatewayType;
	// Google MERCHANTID, Paypal ??
	private String vendorId;

	public String getCheckoutUrl()
	{
		return checkoutUrl;
	}

	public void setCheckoutUrl(String checkoutUrl)
	{
		this.checkoutUrl = checkoutUrl;
	}

	public String getButtonUrl()
	{
		return buttonUrl;
	}

	public void setButtonUrl(String buttonUrl)
	{
		this.buttonUrl = buttonUrl;
	}

	public String getGatewayType()
	{
		return gatewayType;
	}

	public void setGatewayType(String gatewayType)
	{
		this.gatewayType = gatewayType;
	}

	public String getVendorId()
	{
		return vendorId;
	}

	public void setVendorId(String vendorId)
	{
		this.vendorId = vendorId;
	}
}
