package com.tle.core.payment.beans.store;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

/**
 * @author Aaron
 */
@XmlRootElement
public class StorePricingInformationBean
{
	private boolean allowFree;
	private boolean allowPurchase;
	private boolean allowSubscription;

	private List<StorePurchaseTierBean> purchaseTiers;
	private List<StoreSubscriptionTierBean> subscriptionTiers;
	private List<StoreSubscriptionPeriodBean> subscriptionPeriods;

	private boolean subscriptionPerUser;
	private boolean purchasePerUser;

	private List<StorePaymentGatewayBean> paymentGateways;

	private String defaultCurrency;
	private StoreTaxBean tax;

	private Map<String, StoreSubscriptionPeriodBean> subscriptionPeriodMap;

	public boolean isAllowFree()
	{
		return allowFree;
	}

	public void setAllowFree(boolean allowFree)
	{
		this.allowFree = allowFree;
	}

	public boolean isAllowPurchase()
	{
		return allowPurchase;
	}

	public void setAllowPurchase(boolean allowPurchase)
	{
		this.allowPurchase = allowPurchase;
	}

	public boolean isAllowSubscription()
	{
		return allowSubscription;
	}

	public void setAllowSubscription(boolean allowSubscription)
	{
		this.allowSubscription = allowSubscription;
	}

	public List<StorePurchaseTierBean> getPurchaseTiers()
	{
		return purchaseTiers;
	}

	public void setPurchaseTiers(List<StorePurchaseTierBean> purchaseTiers)
	{
		this.purchaseTiers = purchaseTiers;
	}

	public List<StoreSubscriptionTierBean> getSubscriptionTiers()
	{
		return subscriptionTiers;
	}

	public void setSubscriptionTiers(List<StoreSubscriptionTierBean> subscriptionTiers)
	{
		this.subscriptionTiers = subscriptionTiers;
	}

	public List<StoreSubscriptionPeriodBean> getSubscriptionPeriods()
	{
		return subscriptionPeriods;
	}

	public void setSubscriptionPeriods(List<StoreSubscriptionPeriodBean> subscriptionPeriods)
	{
		this.subscriptionPeriods = subscriptionPeriods;
	}

	public boolean isSubscriptionPerUser()
	{
		return subscriptionPerUser;
	}

	public void setSubscriptionPerUser(boolean subscriptionPerUser)
	{
		this.subscriptionPerUser = subscriptionPerUser;
	}

	public boolean isPurchasePerUser()
	{
		return purchasePerUser;
	}

	public void setPurchasePerUser(boolean purchasePerUser)
	{
		this.purchasePerUser = purchasePerUser;
	}

	public List<StorePaymentGatewayBean> getPaymentGateways()
	{
		return paymentGateways;
	}

	public void setPaymentGateways(List<StorePaymentGatewayBean> paymentGateways)
	{
		this.paymentGateways = paymentGateways;
	}

	public String getDefaultCurrency()
	{
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency)
	{
		this.defaultCurrency = defaultCurrency;
	}

	public StoreTaxBean getTax()
	{
		return tax;
	}

	public void setTax(StoreTaxBean tax)
	{
		this.tax = tax;
	}

	@JsonIgnore
	public StoreSubscriptionPeriodBean getSubscriptionPeriod(String uuid)
	{
		if( subscriptionPeriodMap == null )
		{
			subscriptionPeriodMap = Maps.newHashMap();
			if( subscriptionPeriods != null )
			{
				for( StoreSubscriptionPeriodBean sp : subscriptionPeriods )
				{
					subscriptionPeriodMap.put(sp.getUuid(), sp);
				}
			}
		}
		return subscriptionPeriodMap.get(uuid);
	}
}
