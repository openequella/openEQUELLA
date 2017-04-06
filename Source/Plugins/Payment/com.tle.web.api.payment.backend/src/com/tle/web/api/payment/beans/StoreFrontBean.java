package com.tle.web.api.payment.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tle.common.interfaces.I18NString;
import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.oauth.interfaces.beans.OAuthClientBean;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreFrontBean extends BaseEntityBean
{
	private boolean free;
	private boolean purchase;
	private boolean subscription;
	private String country;
	private String product;
	private String productVersion;
	private String contactPhone;
	private boolean enabled;
	private OAuthClientBean client;

	// Renamed entity field
	public I18NString getNotes()
	{
		return super.getDescription();
	}

	// Renamed entity field
	public void setNotes(I18NString notes)
	{
		super.setDescription(notes);
	}

	@JsonIgnore
	@Override
	public I18NString getDescription()
	{
		return null;
	}

	@JsonIgnore
	@Override
	public void setDescription(I18NString description)
	{
		// Nada
	}

	public boolean isFree()
	{
		return free;
	}

	public void setFree(boolean free)
	{
		this.free = free;
	}

	public boolean isPurchase()
	{
		return purchase;
	}

	public void setPurchase(boolean purchase)
	{
		this.purchase = purchase;
	}

	public boolean isSubscription()
	{
		return subscription;
	}

	public void setSubscription(boolean subscription)
	{
		this.subscription = subscription;
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

	public String getContactPhone()
	{
		return contactPhone;
	}

	public void setContactPhone(String contactPhone)
	{
		this.contactPhone = contactPhone;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public OAuthClientBean getClient()
	{
		return client;
	}

	public void setClient(OAuthClientBean client)
	{
		this.client = client;
	}
}
