package com.tle.common.payment.storefront.entity;

import java.io.Serializable;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.common.DoNotSimplify;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
public class OrderStorePart implements Serializable
{
	private static final long serialVersionUID = 1L;

	/*
	 * Do not re-order this enum!
	 */
	public enum PaidStatus
	{
		NONE, PAID,
		/**
		 * Paypal (or whatever) is holding off
		 */
		PENDING
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	/**
	 * Used a customer reference
	 */
	@Column(length = 40, nullable = false)
	private String uuid;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "orderitemStoreIndex")
	private Store store;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderColumn(name = "orderitempos", nullable = false)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "order_store_part_id", nullable = false)
	private List<OrderItem> orderItems = Lists.newArrayList();

	// backref
	@DoNotSimplify
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "orderstoreOrderIndex")
	private Order order;

	@Column(nullable = false)
	@Min(0)
	private long price;

	@Column(nullable = false)
	@Min(0)
	private long tax;

	@Column(length = 10, nullable = true)
	private String taxCode;

	@Column(length = 10, nullable = true)
	private Currency currency;

	@Column(nullable = false)
	private int paid;

	@Column(nullable = true)
	private Date paidDate;

	@Column(length = 40, nullable = true)
	private String checkoutUuid;

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

	public List<OrderItem> getOrderItems()
	{
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems)
	{
		this.orderItems = orderItems;
	}

	public Store getStore()
	{
		return store;
	}

	public void setStore(Store store)
	{
		this.store = store;
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

	public Currency getCurrency()
	{
		return currency;
	}

	public void setCurrency(Currency currency)
	{
		this.currency = currency;
	}

	public Order getOrder()
	{
		return order;
	}

	public void setOrder(Order order)
	{
		this.order = order;
	}

	public void setPaid(PaidStatus paid)
	{
		this.paid = paid.ordinal();
	}

	public PaidStatus getPaid()
	{
		return PaidStatus.values()[paid];
	}

	public Date getPaidDate()
	{
		return paidDate;
	}

	public void setPaidDate(Date paidDate)
	{
		this.paidDate = paidDate;
	}

	public String getCheckoutUuid()
	{
		return checkoutUuid;
	}

	public void setCheckoutUuid(String checkoutUuid)
	{
		this.checkoutUuid = checkoutUuid;
	}
}
