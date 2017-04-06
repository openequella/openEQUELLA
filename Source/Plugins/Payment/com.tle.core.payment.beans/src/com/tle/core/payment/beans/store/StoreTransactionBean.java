package com.tle.core.payment.beans.store;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreTransactionBean
{
	public enum PaidStatus
	{
		SUBMITTED, PAID, PENDING
	}

	private String uuid;
	private String customerReference;
	private StorePriceBean price;
	private String receipt;
	private List<StoreTransactionItemBean> items;

	private Date creationDate;
	private PaidStatus paidStatus;
	private Date paidDate;

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public String getCustomerReference()
	{
		return customerReference;
	}

	public void setCustomerReference(String customerReference)
	{
		this.customerReference = customerReference;
	}

	public StorePriceBean getPrice()
	{
		return price;
	}

	public void setPrice(StorePriceBean price)
	{
		this.price = price;
	}

	public String getReceipt()
	{
		return receipt;
	}

	public void setReceipt(String receipt)
	{
		this.receipt = receipt;
	}

	public List<StoreTransactionItemBean> getItems()
	{
		return items;
	}

	public void setItems(List<StoreTransactionItemBean> items)
	{
		this.items = items;
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
		return paidStatus;
	}

	public void setPaidStatus(PaidStatus paidStatus)
	{
		this.paidStatus = paidStatus;
	}

	public Date getPaidDate()
	{
		return paidDate;
	}

	public void setPaidDate(Date paidDate)
	{
		this.paidDate = paidDate;
	}
}
