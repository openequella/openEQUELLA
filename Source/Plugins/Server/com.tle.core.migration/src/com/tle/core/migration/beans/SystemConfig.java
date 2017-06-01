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

package com.tle.core.migration.beans;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.AccessType;

/**
 * This class is only held in this plug-in to facilitate the setting-up and
 * checking of the administration password for migrations.
 */
@Entity
@AccessType("field")
@SuppressWarnings("nls")
@Table(name = SystemConfig.TABLE_NAME)
public class SystemConfig implements Serializable
{
	private static final long serialVersionUID = -1L;

	public static final String TABLE_NAME = "sys_system_config";
	public static final String ADMIN_PASSWORD = "admin.password";

	@Id
	private String key;
	@Lob
	private String value;

	public SystemConfig()
	{
		// Nothing to do here
	}

	public SystemConfig(String key)
	{
		this.key = key;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof SystemConfig) )
		{
			return false;
		}

		return Objects.equals(key, ((SystemConfig) obj).key);
	}
}
