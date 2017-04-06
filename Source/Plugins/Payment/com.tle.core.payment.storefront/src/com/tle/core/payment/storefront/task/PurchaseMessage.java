package com.tle.core.payment.storefront.task;

import java.io.Serializable;

public class PurchaseMessage implements Serializable
{
	public enum Type
	{
		ORDERS, DOWNLOADS
	}

	private static final long serialVersionUID = 1L;

	private Type type;
	private final long institutionId;

	public PurchaseMessage(Type type, long institutionId)
	{
		this.type = type;
		this.institutionId = institutionId;
	}

	public Type getType()
	{
		return type;
	}

	public long getInstitutionId()
	{
		return institutionId;
	}
}
