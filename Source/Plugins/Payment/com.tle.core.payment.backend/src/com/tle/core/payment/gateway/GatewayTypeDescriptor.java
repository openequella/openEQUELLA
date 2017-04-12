package com.tle.core.payment.gateway;

import java.io.Serializable;

public class GatewayTypeDescriptor implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String type;
	private final String nameKey;
	private final String descriptionKey;

	public GatewayTypeDescriptor(String type, String nameKey, String descriptionKey)
	{
		super();
		this.type = type;
		this.nameKey = nameKey;
		this.descriptionKey = descriptionKey;
	}

	public String getType()
	{
		return type;
	}

	public String getNameKey()
	{
		return nameKey;
	}

	public String getDescriptionKey()
	{
		return descriptionKey;
	}

}
