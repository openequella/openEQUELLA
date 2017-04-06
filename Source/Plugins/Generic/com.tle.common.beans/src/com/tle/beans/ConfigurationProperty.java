/*
 * Created on Oct 27, 2005
 */
package com.tle.beans;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.google.common.base.Preconditions;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class ConfigurationProperty
{
	@SuppressWarnings("nls")
	public static final String TABLE_NAME = "configuration_property";

	@EmbeddedId
	private PropertyKey key;
	@Lob
	private String value;

	public ConfigurationProperty()
	{
		super();
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public PropertyKey getKey()
	{
		return key;
	}

	public void setKey(PropertyKey key)
	{
		this.key = key;
	}

	public static class PropertyKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String property;
		private long institutionId;

		public PropertyKey()
		{
			super();
		}

		public PropertyKey(Institution institution, String property)
		{
			setInstitution(institution);
			this.property = property;
		}

		public PropertyKey(long instDbId, String property)
		{
			setInstitutionId(instDbId);
			this.property = property;
		}

		public String getProperty()
		{
			return property;
		}

		public void setProperty(String property)
		{
			this.property = property;
		}

		public void setInstitution(Institution institution)
		{
			Preconditions.checkNotNull(institution);
			institutionId = institution.getDatabaseId();
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof PropertyKey) )
			{
				return false;
			}

			PropertyKey pkey = (PropertyKey) obj;
			return pkey.institutionId == institutionId && property.equals(pkey.property);
		}

		@Override
		public int hashCode()
		{
			return Long.valueOf(institutionId).hashCode() + property.hashCode();
		}

		public long getInstitutionId()
		{
			return institutionId;
		}

		public void setInstitutionId(long institutionId)
		{
			this.institutionId = institutionId;
		}
	}
}
