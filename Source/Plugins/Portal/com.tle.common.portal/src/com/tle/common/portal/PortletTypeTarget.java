package com.tle.common.portal;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class PortletTypeTarget implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String PORTLET_TYPE_TARGET_PREFIX = "P:";

	private final String type;

	public PortletTypeTarget(String type)
	{
		this.type = type;
	}

	public String getDisplayName()
	{
		// TODO: no, not really
		return type;
	}

	public String getTarget()
	{
		return PORTLET_TYPE_TARGET_PREFIX + type;
	}

	@Override
	public boolean equals(Object other)
	{
		if( !(other instanceof PortletTypeTarget) )
		{
			return false;
		}
		return Objects.equals(((PortletTypeTarget) other).type, type);
	}

	@Override
	public int hashCode()
	{
		return type.hashCode();
	}
}
