package com.tle.common.connectors;

import java.io.Serializable;

/**
 * Just a simple bean for transport around the place
 * 
 * @author aholland
 */
public class ConnectorTypeDescriptor implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final String type;
	private final String nameKey;
	private final String descriptionKey;

	public ConnectorTypeDescriptor(String type, String nameKey, String descriptionKey)
	{
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
