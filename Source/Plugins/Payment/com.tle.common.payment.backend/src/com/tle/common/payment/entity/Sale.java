package com.tle.common.payment.entity;

import java.math.BigDecimal;
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
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;

@Entity
@AccessType("field")
public class Sale
{
	/*
	 * Do not re-order this enum!
	 */
	public enum PaidStatus
	{
		NONE, PAID, PENDING
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "saleUuidIndex")
	private String uuid;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "saleInstitutionIndex")
	private Institution institution;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderColumn(name = "saleitempos", nullable = false)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "sale_id", nullable = false)
	private List<SaleItem> sales;

	@Column(nullable = false)
	@Min(0)
	private long price;

	@Column(nullable = false)
	@Min(0)
	private long tax;

	@Column(precision = 9, scale = 4)
	@Min(0)
	private BigDecimal taxPercent;

	@Column(length = 10, nullable = true)
	private String taxCode;

	@Column(length = 10, nullable = false)
	private Currency currency;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@Index(name = "saleStorefrontIndex")
	private StoreFront storefront;

	@Column(length = 40, nullable = true)
	private String paymentGatewayUuid;

	@Column(nullable = false)
	private Date creationDate;

	@Column(nullable = true)
	private Date paidDate;

	@Column(nullable = false)
	private int paidStatus;

	@Column(nullable = true)
	private String receipt;

	// Misc attributes returned by payment gateway
	@Lob
	private String data;

	@Column(length = 40, nullable = false)
	@Index(name = "saleCustrefIndex")
	private String customerReference;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public List<SaleItem> getSales()
	{
		return sales;
	}

	public void setSales(List<SaleItem> sales)
	{
		this.sales = sales;
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

	public BigDecimal getTaxPercent()
	{
		return taxPercent;
	}

	public void setTaxPercent(BigDecimal taxPercent)
	{
		this.taxPercent = taxPercent;
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

	public StoreFront getStorefront()
	{
		return storefront;
	}

	public void setStorefront(StoreFront storefront)
	{
		this.storefront = storefront;
	}

	public String getPaymentGatewayUuid()
	{
		return paymentGatewayUuid;
	}

	public void setPaymentGatewayUuid(String paymentGatewayUuid)
	{
		this.paymentGatewayUuid = paymentGatewayUuid;
	}

	public Date getCreationDate()
	{
		return creationDate;
	}

	public void setCreationDate(Date creationDate)
	{
		this.creationDate = creationDate;
	}

	public PaidStatus getPaidStatus()
	{
		return PaidStatus.values()[paidStatus];
	}

	public void setPaidStatus(PaidStatus paidStatus)
	{
		this.paidStatus = paidStatus.ordinal();
	}

	public Date getPaidDate()
	{
		return paidDate;
	}

	public void setPaidDate(Date paidDate)
	{
		this.paidDate = paidDate;
	}

	public String getReceipt()
	{
		return receipt;
	}

	public void setReceipt(String receipt)
	{
		this.receipt = receipt;
	}

	public String getCustomerReference()
	{
		return customerReference;
	}

	public void setCustomerReference(String customerReference)
	{
		this.customerReference = customerReference;
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}
}
