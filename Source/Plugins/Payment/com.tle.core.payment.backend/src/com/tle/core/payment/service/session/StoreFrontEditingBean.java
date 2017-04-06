package com.tle.core.payment.service.session;

import com.tle.common.payment.entity.TaxType;
import com.tle.core.oauth.service.OAuthClientEditingBean;
import com.tle.core.services.entity.EntityEditingBean;

public class StoreFrontEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private boolean allowFree;
	private boolean allowPurchase;
	private boolean allowSubscription;
	private String country;
	private String product;
	private String productVersion;
	private String productUrl;
	private String contactPhone;
	private OAuthClientEditingBean client = new OAuthClientEditingBean();
	private TaxType taxType;

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

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getProduct()
	{
		return product;
	}

	public void setProduct(String product)
	{
		this.product = product;
	}

	public String getProductVersion()
	{
		return productVersion;
	}

	public void setProductVersion(String productVersion)
	{
		this.productVersion = productVersion;
	}

	public String getProductUrl()
	{
		return productUrl;
	}

	public void setProductUrl(String productUrl)
	{
		this.productUrl = productUrl;
	}

	public String getContactPhone()
	{
		return contactPhone;
	}

	public void setContactPhone(String contactPhone)
	{
		this.contactPhone = contactPhone;
	}

	public OAuthClientEditingBean getClient()
	{
		return client;
	}

	public void setClient(OAuthClientEditingBean client)
	{
		this.client = client;
	}

	public TaxType getTaxType()
	{
		return taxType;
	}

	public void setTaxType(TaxType taxType)
	{
		this.taxType = taxType;
	}
}