package com.tle.common.payment.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.oauth.beans.OAuthClient;

@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"client_id"})})
@Entity
@AccessType("field")
public class StoreFront extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	private boolean allowFree;
	private boolean allowPurchase;
	private boolean allowSubscription;

	@Column(nullable = false, length = 4)
	private String country;
	@Column(nullable = false, length = 1024)
	private String product;
	@Column(nullable = false, length = 64)
	private String productVersion;
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@Index(name = "storefrontClientIndex")
	private OAuthClient client;
	@Column(length = 30)
	private String contactPhone;
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "taxTypeIndex")
	private TaxType taxType;

	public TaxType getTaxType()
	{
		return taxType;
	}

	public void setTaxType(TaxType taxType)
	{
		this.taxType = taxType;
	}

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

	public OAuthClient getClient()
	{
		return client;
	}

	public void setClient(OAuthClient client)
	{
		this.client = client;
	}

	public String getContactPhone()
	{
		return contactPhone;
	}

	public void setContactPhone(String contactPhone)
	{
		this.contactPhone = contactPhone;
	}
}
