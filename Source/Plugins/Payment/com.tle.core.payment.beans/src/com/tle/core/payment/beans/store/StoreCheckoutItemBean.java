package com.tle.core.payment.beans.store;

import java.util.Date;

/**
 * @author Aaron
 */
public class StoreCheckoutItemBean
{
	/**
	 * Do not supply this
	 */
	private String uuid;

	private String itemUuid;

	/**
	 * Validation only
	 */
	private int itemVersion;

	private int quantity;

	/**
	 * Note that if this and the purchase tier is missing then the customer is
	 * requesting it for free, which may or may not be valid
	 */
	private StoreSubscriptionTierBean subscriptionTier;

	private StorePurchaseTierBean purchaseTier;

	/**
	 * (if the susbscriptionTier is supplied)
	 */
	private StoreSubscriptionPeriodBean subscriptionPeriod;

	private Date subscriptionStartDate;

	/**
	 * When a checkout is submitted, this is the _expected_ price, and _must_ be
	 * supplied.
	 */
	private StorePriceBean price;

	/**
	 * When a checkout is submitted, this is the _expected_ price, and _must_ be
	 * supplied.
	 */
	private StorePriceBean unitPrice;

	private String catalogueUuid;

	/**
	 * Readonly
	 * 
	 * @return
	 */
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getItemUuid()
	{
		return itemUuid;
	}

	public void setItemUuid(String itemUuid)
	{
		this.itemUuid = itemUuid;
	}

	public int getItemVersion()
	{
		return itemVersion;
	}

	public void setItemVersion(int itemVersion)
	{
		this.itemVersion = itemVersion;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public StoreSubscriptionTierBean getSubscriptionTier()
	{
		return subscriptionTier;
	}

	public void setSubscriptionTier(StoreSubscriptionTierBean subscriptionTier)
	{
		this.subscriptionTier = subscriptionTier;
	}

	public StorePurchaseTierBean getPurchaseTier()
	{
		return purchaseTier;
	}

	public void setPurchaseTier(StorePurchaseTierBean purchaseTier)
	{
		this.purchaseTier = purchaseTier;
	}

	public void setSubscriptionPeriod(StoreSubscriptionPeriodBean subscriptionPeriod)
	{
		this.subscriptionPeriod = subscriptionPeriod;
	}

	public StoreSubscriptionPeriodBean getSubscriptionPeriod()
	{
		return subscriptionPeriod;
	}

	public Date getSubscriptionStartDate()
	{
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(Date subscriptionStartDate)
	{
		this.subscriptionStartDate = subscriptionStartDate;
	}

	public StorePriceBean getPrice()
	{
		return price;
	}

	public void setPrice(StorePriceBean price)
	{
		this.price = price;
	}

	public StorePriceBean getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice(StorePriceBean unitPrice)
	{
		this.unitPrice = unitPrice;
	}

	public String getCatalogueUuid()
	{
		return catalogueUuid;
	}

	public void setCatalogueUuid(String catalogueUuid)
	{
		this.catalogueUuid = catalogueUuid;
	}
}
