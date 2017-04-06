package com.tle.core.payment.beans.store;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreCheckoutBean
{
	public enum PaidStatus
	{
		SUBMITTED, PAID, PENDING
	}

	private String uuid;
	/**
	 * A string that the store front can add arbitrary data to
	 */
	private String customerReference;

	/**
	 * When a checkout is submitted, this is the _expected_ price, and _must_ be
	 * supplied. It prevents a rude shock to the Store Front. If the expected
	 * total doesn't match the total calculated by the store then an error is
	 * thrown
	 */
	private StorePriceBean price;

	private List<StoreCheckoutItemBean> items;

	private Date creationDate;

	/**
	 * "submitted" - submitted but not paid "paid" "pending" ...others?
	 */
	private PaidStatus paidStatus;

	/**
	 * UUID is assigned by the store
	 */
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	/**
	 * A string that the store front can add arbitrary data to
	 */
	public String getCustomerReference()
	{
		return customerReference;
	}

	public void setCustomerReference(String customerReference)
	{
		this.customerReference = customerReference;
	}

	/**
	 * If the expected total doesn't match the total calculated by the store
	 * then an error is thrown
	 */
	public StorePriceBean getPrice()
	{
		return price;
	}

	public void setPrice(StorePriceBean price)
	{
		this.price = price;
	}

	/**
	 * The list of StoreCheckoutItemBean (s) in the checkout
	 */
	public List<StoreCheckoutItemBean> getItems()
	{
		return items;
	}

	public void setItems(List<StoreCheckoutItemBean> items)
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
}
