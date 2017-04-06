package com.tle.core.payment.service.session;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.core.services.entity.EntityEditingBean;

/**
 * Wave of the future Dude. 100% electronic.
 */
public class PricingTierEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private boolean purchase;
	private PricingTierEditingBean.PriceBean purchasePrice;
	private final List<PricingTierEditingBean.PriceBean> subscriptionPrices = Lists.newArrayList();

	public boolean isPurchase()
	{
		return purchase;
	}

	public void setPurchase(boolean purchase)
	{
		this.purchase = purchase;
	}

	public PricingTierEditingBean.PriceBean getPurchasePrice()
	{
		return purchasePrice;
	}

	public void setPurchasePrice(PricingTierEditingBean.PriceBean purchasePrice)
	{
		this.purchasePrice = purchasePrice;
	}

	public List<PricingTierEditingBean.PriceBean> getSubscriptionPrices()
	{
		return subscriptionPrices;
	}

	public static class PriceBean implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private long id;

		private boolean enabled = true;
		private String value;
		private long subscriptionPeriodId;
		private String currency;

		// currently unused
		private long catalogueId;
		private long regionId;

		public void setId(long id)
		{
			this.id = id;
		}

		public boolean isEnabled()
		{
			return enabled;
		}

		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}

		public String getValue()
		{
			return value;
		}

		public void setValue(String value)
		{
			this.value = value;
		}

		public String getCurrency()
		{
			return currency;
		}

		public void setCurrency(String currency)
		{
			this.currency = currency;
		}

		public long getSubscriptionPeriodId()
		{
			return subscriptionPeriodId;
		}

		public void setSubscriptionPeriodId(long subscriptionPeriodId)
		{
			this.subscriptionPeriodId = subscriptionPeriodId;
		}

		public long getCatalogueId()
		{
			return catalogueId;
		}

		public void setCatalogueId(long catalogueId)
		{
			this.catalogueId = catalogueId;
		}

		public long getRegionId()
		{
			return regionId;
		}

		public void setRegionId(long regionId)
		{
			this.regionId = regionId;
		}

		public long getId()
		{
			return id;
		}
	}
}