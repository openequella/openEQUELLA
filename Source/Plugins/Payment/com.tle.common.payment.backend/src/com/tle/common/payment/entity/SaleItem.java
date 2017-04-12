package com.tle.common.payment.entity;

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
public class SaleItem
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "saleitemUuidIndex")
	private String uuid;

	@Column(length = 40, nullable = false)
	@Index(name = "saleitemItemuuidIndex")
	private String itemUuid;

	@Column(length = 40, nullable = false)
	@Index(name = "saleitemCatalogueuuidIndex")
	private String catalogueUuid;

	/**
	 * Version of the item at the time it was bought. Informational only.
	 */
	private int itemVersion;

	/*
	 * Name of the item at the time it was bought?
	 */
	// private String itemName;
	@Column(nullable = false)
	@Min(0)
	private int quantity;

	@Column(nullable = false)
	@Min(0)
	private long price;

	@Column(nullable = false)
	@Min(0)
	private long unitPrice;

	@Column(nullable = false)
	@Min(0)
	private long tax;

	@Column(nullable = false)
	@Min(0)
	private long unitTax;

	@Column(length = 10, nullable = true)
	private String taxCode;

	/**
	 * Note: if pricingTier is absent, item was obtained for free.
	 */
	@Column(length = 40)
	private String pricingTierUuid;

	/**
	 * We will need to allow disabling of subscription periods, otherwise you
	 * could never get rid of them once a sale is made.
	 */
	@ManyToOne(optional = true)
	@Index(name = "si_period")
	private SubscriptionPeriod period;

	@Column(nullable = true)
	private Date subscriptionStartDate;

	/**
	 * Calendar variation makes for setting a fixed end date whenever start date
	 * is set, in preference to forever recalculating startdate + (duration *
	 * durationUnit)
	 */
	@Column(nullable = true)
	private Date subscriptionEndDate;

	// backref
	@DoNotSimplify
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sale_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "saleitemSaleIndex")
	private Sale sale;

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

	public int getItemVersion()
	{
		return itemVersion;
	}

	public void setItemVersion(int itemVersion)
	{
		this.itemVersion = itemVersion;
	}

	public String getItemUuid()
	{
		return itemUuid;
	}

	public void setItemUuid(String itemUuid)
	{
		this.itemUuid = itemUuid;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public void setQuantity(int quantity)
	{
		this.quantity = quantity;
	}

	public long getPrice()
	{
		return price;
	}

	public void setPrice(long price)
	{
		this.price = price;
	}

	public long getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice(long unitPrice)
	{
		this.unitPrice = unitPrice;
	}

	public long getTax()
	{
		return tax;
	}

	public void setTax(long tax)
	{
		this.tax = tax;
	}

	public long getUnitTax()
	{
		return unitTax;
	}

	public void setUnitTax(long unitTax)
	{
		this.unitTax = unitTax;
	}

	public String getTaxCode()
	{
		return taxCode;
	}

	public void setTaxCode(String taxCode)
	{
		this.taxCode = taxCode;
	}

	public String getPricingTierUuid()
	{
		return pricingTierUuid;
	}

	public void setPricingTierUuid(String pricingTierUuid)
	{
		this.pricingTierUuid = pricingTierUuid;
	}

	public SubscriptionPeriod getPeriod()
	{
		return period;
	}

	public void setPeriod(SubscriptionPeriod period)
	{
		this.period = period;
	}

	public Sale getSale()
	{
		return sale;
	}

	public void setSale(Sale sale)
	{
		this.sale = sale;
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
}
