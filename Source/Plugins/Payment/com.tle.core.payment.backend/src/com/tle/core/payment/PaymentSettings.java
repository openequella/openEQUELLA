package com.tle.core.payment;

import java.util.Currency;
import java.util.Locale;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

/**
 * @author Aaron
 */
public class PaymentSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1L;

	@Property(key = "payment.free.enabled")
	private boolean freeEnabled;

	@Property(key = "payment.purchase.enabled")
	private boolean purchaseEnabled;
	@Property(key = "payment.purchase.flatrate")
	private boolean purchaseFlatRate;

	@Property(key = "payment.subscription.enabled")
	private boolean subscriptionEnabled;
	@Property(key = "payment.subscription.flatrate")
	private boolean subscriptionFlatRate;

	/**
	 * May be refactored out when we have per region pricing
	 */
	@Property(key = "payment.currency")
	private volatile String currency;

	public boolean isFreeEnabled()
	{
		return freeEnabled;
	}

	public void setFreeEnabled(boolean freeEnabled)
	{
		this.freeEnabled = freeEnabled;
	}

	public boolean isPurchaseEnabled()
	{
		return purchaseEnabled;
	}

	public void setPurchaseEnabled(boolean purchaseEnabled)
	{
		this.purchaseEnabled = purchaseEnabled;
	}

	public boolean isPurchaseFlatRate()
	{
		return purchaseFlatRate;
	}

	public void setPurchaseFlatRate(boolean purchaseFlatRate)
	{
		this.purchaseFlatRate = purchaseFlatRate;
	}

	public boolean isSubscriptionEnabled()
	{
		return subscriptionEnabled;
	}

	public void setSubscriptionEnabled(boolean subscriptionEnabled)
	{
		this.subscriptionEnabled = subscriptionEnabled;
	}

	public boolean isSubscriptionFlatRate()
	{
		return subscriptionFlatRate;
	}

	public void setSubscriptionFlatRate(boolean subscriptionFlatRate)
	{
		this.subscriptionFlatRate = subscriptionFlatRate;
	}

	public String getCurrency()
	{
		if( currency == null )
		{
			synchronized( this )
			{
				if( currency == null )
				{
					// Uses *server* default locale, which *may* be en_US so you
					// will get USD.
					// Sad panda.
					currency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
				}
			}
		}
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}
}