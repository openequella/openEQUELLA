package com.tle.beans.entity;

import java.io.Serializable;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.i18n.BundleReference;

/**
 * @author Nicholas Read
 */
public class BaseEntityLabel implements Serializable, FieldEquality<BaseEntityLabel>, BundleReference
{
	private static final long serialVersionUID = 1L;

	private final long id;
	private final long bundleId;
	private final String uuid;
	private final String owner;
	private final boolean systemType;

	private String privType;

	public BaseEntityLabel(long id, String uuid, long bundleId, String owner, boolean systemType)
	{
		this.id = id;
		this.uuid = uuid;
		this.bundleId = bundleId;
		this.owner = owner;
		this.systemType = systemType;
	}

	public long getId()
	{
		return id;
	}

	public String getUuid()
	{
		return uuid;
	}

	@Override
	public long getBundleId()
	{
		return bundleId;
	}

	public String getOwner()
	{
		return owner;
	}

	public boolean isSystemType()
	{
		return systemType;
	}

	public void setPrivType(String privType)
	{
		this.privType = privType;
	}

	public String getPrivType()
	{
		return privType;
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	/*
	 * (non-Javadoc)
	 * @see com.tle.common.Check.FieldEquality#checkFields(java.lang.Object)
	 */
	@Override
	public boolean checkFields(BaseEntityLabel rhs)
	{
		return id == rhs.getId();
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(id).hashCode();
	}

	@Override
	public long getIdValue()
	{
		return id;
	}

	@Override
	public String getValue()
	{
		return Long.toString(id);
	}
}
