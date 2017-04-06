package com.tle.core.payment.beans.store;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreSubscriptionTierBean extends BaseEntityBean
{
	private boolean perUser;
	private List<StorePriceBean> prices;

	public boolean isPerUser()
	{
		return perUser;
	}

	public void setPerUser(boolean perUser)
	{
		this.perUser = perUser;
	}

	public List<StorePriceBean> getPrices()
	{
		return prices;
	}

	public void setPrices(List<StorePriceBean> prices)
	{
		this.prices = prices;
	}
}
