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

package com.tle.common.portal.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.entity.BaseEntity;

/**
 * @author aholland
 */
@Entity
@AccessType("field")
public final class Portlet extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@Column(length = 16, nullable = false)
	private String type;
	private boolean closeable;
	private boolean minimisable;
	private boolean enabled;
	@Index(name = "portletInstitutional")
	private boolean institutional;
	@Lob
	private String config;
	@Transient
	private Object extraData;

	public Portlet()
	{
		// for hibernate
	}

	public Portlet(String type)
	{
		this.type = type;
	}

	public boolean isCloseable()
	{
		return closeable;
	}

	public void setCloseable(boolean closeable)
	{
		this.closeable = closeable;
	}

	public boolean isMinimisable()
	{
		return minimisable;
	}

	public void setMinimisable(boolean minimisable)
	{
		this.minimisable = minimisable;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @return true if this was created by an admin for a set of users across an
	 *         institution to be able to see. (i.e. this is not a user created
	 *         portlet)
	 */
	public boolean isInstitutional()
	{
		return institutional;
	}

	public void setInstitutional(boolean institutional)
	{
		this.institutional = institutional;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	public Object getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Object extraData)
	{
		this.extraData = extraData;
	}

}
