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
import com.tle.beans.Institution;

@Entity
@AccessType("field")
public class Purchase implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "purchaseUuidIndex")
	private String uuid;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "purchaseInstitutionIndex")
	private Institution institution;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@OrderColumn(name = "purchitempos", nullable = false)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "purchase_id", nullable = false)
	private List<PurchaseItem> purchaseItems = Lists.newArrayList();

	@Column(nullable = false)
	@Min(0)
	private long price;

	@Column(nullable = false)
	@Min(0)
	private long tax;

	@Column(length = 10, nullable = true)
	private String taxCode;

	@Column(length = 10, nullable = false)
	private Currency currency;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "purchaseStoreIndex")
	private Store store;

	@Column(nullable = false)
	private Date checkoutDate;

	// the UUID of the user who built the cart
	@Column(length = 255, nullable = false)
	@Index(name = "purchaseBuyerIndex")
	private String checkoutBy;

	/**
	 * sale UUID on the store's side
	 */
	@Column(length = 255, nullable = false)
	private String transactionUuid;

	@Column(length = 255, nullable = true)
	private String receipt;

	@Column(nullable = true)
	private Date paidDate;

	// the UUID of the user who paid for the cart
	@Column(length = 255)
	private String paidForBy;

	// Perhaps a status instead
	private boolean paid;

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

	public List<PurchaseItem> getPurchaseItems()
	{
		return purchaseItems;
	}

	public void setPurchaseItems(List<PurchaseItem> purchaseItems)
	{
		this.purchaseItems = purchaseItems;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
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

	public Store getStore()
	{
		return store;
	}

	public void setStore(Store store)
	{
		this.store = store;
	}

	public String getTransactionUuid()
	{
		return transactionUuid;
	}

	public void setTransactionUuid(String transactionUuid)
	{
		this.transactionUuid = transactionUuid;
	}

	public String getReceipt()
	{
		return receipt;
	}

	public void setReceipt(String receipt)
	{
		this.receipt = receipt;
	}

	public Date getCheckoutDate()
	{
		return checkoutDate;
	}

	public void setCheckoutDate(Date checkoutDate)
	{
		this.checkoutDate = checkoutDate;
	}

	public String getCheckoutBy()
	{
		return checkoutBy;
	}

	public void setCheckoutBy(String checkoutBy)
	{
		this.checkoutBy = checkoutBy;
	}

	public Date getPaidDate()
	{
		return paidDate;
	}

	public void setPaidDate(Date paidDate)
	{
		this.paidDate = paidDate;
	}

	public String getPaidForBy()
	{
		return paidForBy;
	}

	public void setPaidForBy(String paidForBy)
	{
		this.paidForBy = paidForBy;
	}

	public boolean isPaid()
	{
		return paid;
	}

	public void setPaid(boolean paid)
	{
		this.paid = paid;
	}

}
