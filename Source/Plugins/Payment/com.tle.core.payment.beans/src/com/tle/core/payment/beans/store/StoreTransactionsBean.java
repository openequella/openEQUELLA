package com.tle.core.payment.beans.store;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Aaron
 */
@XmlRootElement
public class StoreTransactionsBean
{
	private int start;
	private int length;
	private int available;
	private List<StoreTransactionBean> transactions;

	public int getStart()
	{
		return start;
	}

	public void setStart(int start)
	{
		this.start = start;
	}

	public int getLength()
	{
		return length;
	}

	public void setLength(int length)
	{
		this.length = length;
	}

	public int getAvailable()
	{
		return available;
	}

	public void setAvailable(int available)
	{
		this.available = available;
	}

	public List<StoreTransactionBean> getTransactions()
	{
		return transactions;
	}

	public void setTransactions(List<StoreTransactionBean> transactions)
	{
		this.transactions = transactions;
	}
}
