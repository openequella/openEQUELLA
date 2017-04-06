package com.tle.core.payment.operation;

import java.io.Serializable;

/**
 * @author Aaron
 */
public class ChangeTierState implements Serializable
{
	private final boolean free;
	private final String purchaseUuid;
	private final String subscriptionUuid;

	public ChangeTierState(boolean free, String purchaseUuid, String subscriptionUuid)
	{
		this.free = free;
		this.purchaseUuid = purchaseUuid;
		this.subscriptionUuid = subscriptionUuid;
	}

	public boolean isFree()
	{
		return free;
	}

	public String getPurchaseUuid()
	{
		return purchaseUuid;
	}

	public String getSubscriptionUuid()
	{
		return subscriptionUuid;
	}
}
