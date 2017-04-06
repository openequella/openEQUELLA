package com.tle.common.payment.storefront.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.common.DoNotSimplify;

@Entity
@AccessType("field")
public class PurchaseItem implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "purchitemUuidIndex")
	private String uuid;

	@Column(length = 40, nullable = false)
	private String sourceItemUuid;

	@Min(0)
	private int sourceItemVersion;

	@Column(length = 40, nullable = false)
	private String catalogueUuid;

	// backref
	@DoNotSimplify
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "purchase_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "purchitemPurchaseIndex")
	private Purchase purchase;

	@Column(nullable = false)
	@Min(0)
	private long price;

	@Column(nullable = false)
	@Min(0)
	private long tax;

	@Column(length = 10, nullable = true)
	private String taxCode;

	@Column(nullable = false)
	@Min(0)
	private long unitPrice;

	@Column(nullable = false)
	@Min(0)
	private int users;

	@Column(nullable = true)
	private Date subscriptionStartDate;

	@Column(nullable = true)
	private Date subscriptionEndDate;

	// for easy retrieval of end subscription dates (if any) for purchased items
	// private ActivateRequest activation;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getSourceItemUuid()
	{
		return sourceItemUuid;
	}

	public void setSourceItemUuid(String sourceItemUuid)
	{
		this.sourceItemUuid = sourceItemUuid;
	}

	public int getSourceItemVersion()
	{
		return sourceItemVersion;
	}

	public void setSourceItemVersion(int sourceItemVersion)
	{
		this.sourceItemVersion = sourceItemVersion;
	}

	public Purchase getPurchase()
	{
		return purchase;
	}

	public void setPurchase(Purchase purchase)
	{
		this.purchase = purchase;
	}

	public long getPrice()
	{
		return price;
	}

	public void setPrice(long price)
	{
		this.price = price;
	}

	public long getTax()
	{
		return tax;
	}

	public void setTax(long tax)
	{
		this.tax = tax;
	}

	public String getTaxCode()
	{
		return taxCode;
	}

	public void setTaxCode(String taxCode)
	{
		this.taxCode = taxCode;
	}

	public long getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice(long unitPrice)
	{
		this.unitPrice = unitPrice;
	}

	public int getUsers()
	{
		return users;
	}

	public void setUsers(int users)
	{
		this.users = users;
	}

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

	public String getCatalogueUuid()
	{
		return catalogueUuid;
	}

	public void setCatalogueUuid(String catalogueUuid)
	{
		this.catalogueUuid = catalogueUuid;
	}

	// public ActivateRequest getActivation()
	// {
	// return activation;
	// }
	//
	// public void setActivation(ActivateRequest activation)
	// {
	// this.activation = activation;
	// }
}
