package com.tle.core.payment.beans.store;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreTransactionItemBean
{
	private String uuid;
	private StoreHarvestableItemBean item;
	private int quantity;
	private StoreSubscriptionPeriodBean subscriptionPeriod;
	private boolean free;
	private StorePurchaseTierBean purchaseTier;
	private StoreSubscriptionTierBean subscriptionTier;
	private StorePriceBean price;
	private String catalogueUuid;
	private Date subscriptionStartDate;
	private Date subscriptionEndDate;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public StoreHarvestableItemBean getItem()
	{
		return item;
	}

	public void setItem(StoreHarvestableItemBean item)
	{
		this.item = item;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public StoreSubscriptionPeriodBean getSubscriptionPeriod()
	{
		return subscriptionPeriod;
	}

	public void setSubscriptionPeriod(StoreSubscriptionPeriodBean subscriptionPeriod)
	{
		this.subscriptionPeriod = subscriptionPeriod;
	}

	public boolean isFree()
	{
		return free;
	}

	public void setFree(boolean free)
	{
		this.free = free;
	}

	public StorePurchaseTierBean getPurchaseTier()
	{
		return purchaseTier;
	}

	public void setPurchaseTier(StorePurchaseTierBean purchaseTier)
	{
		this.purchaseTier = purchaseTier;
	}

	public StoreSubscriptionTierBean getSubscriptionTier()
	{
		return subscriptionTier;
	}

	public void setSubscriptionTier(StoreSubscriptionTierBean subscriptionTier)
	{
		this.subscriptionTier = subscriptionTier;
	}

	public StorePriceBean getPrice()
	{
		return price;
	}

	public void setPrice(StorePriceBean price)
	{
		this.price = price;
	}

	public String getCatalogueUuid()
	{
		return catalogueUuid;
	}

	public void setCatalogueUuid(String catalogueUuid)
	{
		this.catalogueUuid = catalogueUuid;
	}

	public Date getSubscriptionEndDate()
	{
		return subscriptionEndDate;
	}

	public void setSubscriptionEndDate(Date subscriptionEndDate)
	{
		this.subscriptionEndDate = subscriptionEndDate;
	}

	public Date getSubscriptionStartDate()
	{
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(Date subscriptionStartDate)
	{
		this.subscriptionStartDate = subscriptionStartDate;
	}
}
