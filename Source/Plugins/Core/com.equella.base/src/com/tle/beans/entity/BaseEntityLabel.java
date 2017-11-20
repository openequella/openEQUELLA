/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
