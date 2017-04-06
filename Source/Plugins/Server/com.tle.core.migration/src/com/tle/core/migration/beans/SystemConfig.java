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
