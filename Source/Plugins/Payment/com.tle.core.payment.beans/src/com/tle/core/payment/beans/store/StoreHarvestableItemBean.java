package com.tle.core.payment.beans.store;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.common.payment.entity.StoreHarvestInfo;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreHarvestableItemBean extends EquellaItemBean
{
	private Date subscriptionStartDate;
	private Date subscriptionEndDate;
	private List<StoreHarvestInfo> harvestInfos;

	public Date getSubscriptionStartDate()
	{
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(Date subscriptionStartDate)
	{
		this.subscriptionStartDate = subscriptionStartDate;
	}

	public Date getSubscriptionEndDate()
	{
		return subscriptionEndDate;
	}

	public void setSubscriptionEndDate(Date subscriptionEndDate)
	{
		this.subscriptionEndDate = subscriptionEndDate;
	}

	public List<StoreHarvestInfo> getHarvestInfos()
	{
		return harvestInfos;
	}

	public void setHarvestInfos(List<StoreHarvestInfo> harvestInfos)
	{
		this.harvestInfos = harvestInfos;
	}
}
