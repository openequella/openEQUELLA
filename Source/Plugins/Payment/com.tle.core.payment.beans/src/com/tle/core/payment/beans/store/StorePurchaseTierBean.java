package com.tle.core.payment.beans.store;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class StorePurchaseTierBean extends BaseEntityBean
{
	private boolean perUser;
	private StorePriceBean price;

	public boolean isPerUser()
	{
		return perUser;
	}

	public void setPerUser(boolean perUser)
	{
		this.perUser = perUser;
	}

	public StorePriceBean getPrice()
	{
		return price;
	}

	public void setPrice(StorePriceBean price)
	{
		this.price = price;
	}
}
