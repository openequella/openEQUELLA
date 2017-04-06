package com.tle.web.payment.shop;

import java.io.Serializable;
import java.util.Date;

/**
 * Used to remember what the details of the last purchased item were
 */
public class PriceSelection implements Serializable
{
	private final Date start;
	private final boolean subscription;
	private final int numUsers;
	private final String period;

	public PriceSelection(Date start, boolean subscription, int numUsers, String period)
	{
		this.start = start;
		this.subscription = subscription;
		this.numUsers = numUsers;
		this.period = period;
	}

	public Date getStart()
	{
		return start;
	}

	public Boolean getSubscription()
	{
		return subscription;
	}

	public int getNumUsers()
	{
		return numUsers;
	}

	public String getPeriod()
	{
		return period;
	}

}
