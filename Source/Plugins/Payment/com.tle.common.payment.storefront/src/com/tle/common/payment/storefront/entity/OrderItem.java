package com.tle.common.payment.storefront.entity;

import java.io.Serializable;
import java.util.Currency;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.common.DoNotSimplify;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
public class OrderItem implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "orderitemUuidIndex")
	private String uuid;

	@Column(length = 40, nullable = false)
	private String catUuid;

	@Column(length = 40, nullable = false)
	private String itemUuid;

	@Min(0)
	@Column(nullable = false)
	private int itemVersion;

	// Cached name
	@Lob
	private String name;

	@Column(length = 40)
	private String purchaseTierUuid;

	@Column(length = 40)
	private String subscriptionTierUuid;

	@Column(length = 40)
	private String periodUuid;

	@Column
	private boolean perUser;

	@Column(nullable = false)
	@Min(0)
	private int users;

	@Column(nullable = false)
	@Min(0)
	private long unitPrice;

	@Column(nullable = false)
	@Min(0)
	private long unitTax;

	@Column(nullable = false)
	@Min(0)
	private long price;

	@Column(nullable = false)
	@Min(0)
	private long tax;

	@Column(length = 10, nullable = true)
	private String taxCode;

	// Yes, it's nullable, the price could be free
	@Column(length = 10, nullable = true)
	private Currency currency;

	@Column
	private Date addedDate;

	@Column
	private Date subscriptionStartDate;

	// backref
	@DoNotSimplify
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_store_part_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "orderitemOrderstoreIndex")
	private OrderStorePart orderStorePart;

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

	public String getCatUuid()
	{
		return catUuid;
	}

	public void setCatUuid(String catUuid)
	{
		this.catUuid = catUuid;
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

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPurchaseTierUuid()
	{
		return purchaseTierUuid;
	}

	public void setPurchaseTierUuid(String purchaseTierUuid)
	{
		this.purchaseTierUuid = purchaseTierUuid;
	}

	public String getSubscriptionTierUuid()
	{
		return subscriptionTierUuid;
	}

	public void setSubscriptionTierUuid(String subscriptionTierUuid)
	{
		this.subscriptionTierUuid = subscriptionTierUuid;
	}

	public String getPeriodUuid()
	{
		return periodUuid;
	}

	public void setPeriodUuid(String periodUuid)
	{
		this.periodUuid = periodUuid;
	}

	public boolean isPerUser()
	{
		return perUser;
	}

	public void setPerUser(boolean perUser)
	{
		this.perUser = perUser;
	}

	public int getUsers()
	{
		return users;
	}

	public void setUsers(int users)
	{
		this.users = users;
	}

	public long getUnitPrice()
	{
		return unitPrice;
	}

	public void setUnitPrice(long unitPrice)
	{
		this.unitPrice = unitPrice;
	}

	public long getPrice()
	{
		return price;
	}

	public void setPrice(long price)
	{
		this.price = price;
	}

	public long getUnitTax()
	{
		return unitTax;
	}

	public void setUnitTax(long unitTax)
	{
		this.unitTax = unitTax;
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

	public Currency getCurrency()
	{
		return currency;
	}

	public void setCurrency(Currency currency)
	{
		this.currency = currency;
	}

	public Date getAddedDate()
	{
		return addedDate;
	}

	public void setAddedDate(Date addedDate)
	{
		this.addedDate = addedDate;
	}

	public Date getSubscriptionStartDate()
	{
		return subscriptionStartDate;
	}

	public void setSubscriptionStartDate(Date subscriptionStartDate)
	{
		this.subscriptionStartDate = subscriptionStartDate;
	}

	public OrderStorePart getOrderStorePart()
	{
		return orderStorePart;
	}

	public void setOrderStorePart(OrderStorePart orderStorePart)
	{
		this.orderStorePart = orderStorePart;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( obj instanceof OrderItem )
		{
			String otherUuid = ((OrderItem) obj).uuid;
			return Objects.equals(otherUuid, uuid);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return uuid == null ? 0 : uuid.hashCode();
	}
}
