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

package com.tle.core.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.common.i18n.beans.LanguageBundleBean;

/**
 * @author Aaron
 */
public class EntityEditingBean implements Serializable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private String uuid;
	private Long institutionId;
	private String owner;
	private Date dateModified;
	private Date dateCreated;
	private LanguageBundleBean description;
	private LanguageBundleBean name;
	private Map<String, String> attributes;
	private boolean systemType;
	private boolean enabled;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Long getInstitutionId()
	{
		return institutionId;
	}

	public void setInstitutionId(Long institutionId)
	{
		this.institutionId = institutionId;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public Date getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Date dateModified)
	{
		this.dateModified = dateModified;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public LanguageBundleBean getDescription()
	{
		return description;
	}

	public void setDescription(LanguageBundleBean description)
	{
		this.description = description;
	}

	public LanguageBundleBean getName()
	{
		return name;
	}

	public void setName(LanguageBundleBean name)
	{
		this.name = name;
	}

	public Map<String, String> getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes)
	{
		this.attributes = attributes;
	}

	public void setAttribute(String key, String value)
	{
		ensureAttributes();
		attributes.put(key, value);
	}

	public void setAttribute(String key, boolean value)
	{
		ensureAttributes();
		attributes.put(key, Boolean.toString(value));
	}

	private synchronized void ensureAttributes()
	{
		if( attributes == null )
		{
			attributes = Maps.newHashMap();
		}
	}

	public void removeAttribute(String key)
	{
		if( attributes != null )
		{
			attributes.remove(key);
		}
	}

	public String getAttribute(String key)
	{
		if( attributes != null )
		{
			return attributes.get(key);
		}
		return null;
	}

	public boolean getAttribute(String key, boolean defaultValue)
	{
		String val = null;
		if( attributes != null )
		{
			val = attributes.get(key);
		}
		return val == null ? defaultValue : Boolean.valueOf(val);
	}

	public boolean isSystemType()
	{
		return systemType;
	}

	public void setSystemType(boolean systemType)
	{
		this.systemType = systemType;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
}
