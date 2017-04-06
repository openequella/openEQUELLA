/*
 * Created on Oct 25, 2005
 */
package com.tle.beans;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.AccessType;

import com.tle.common.Check;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class UserPreference
{
	@EmbeddedId
	private UserPrefKey key;
	@Lob
	private String data;

	public UserPreference()
	{
		super();
	}

	public String getData()
	{
		return data;
	}

	public void setData(String data)
	{
		this.data = data;
	}

	public UserPrefKey getKey()
	{
		return key;
	}

	public void setKey(UserPrefKey key)
	{
		this.key = key;
	}

	@Embeddable
	@AccessType("field")
	public static class UserPrefKey implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private long institution;
		private String userID;
		@Column(length = 40)
		private String preferenceID;

		public UserPrefKey()
		{
			super();
		}

		public String getUserID()
		{
			return userID;
		}

		public void setUserID(String property)
		{
			this.userID = property;
		}

		public void setInstitution(Institution institution)
		{
			this.institution = institution.getDatabaseId();
		}

		public String getPreferenceID()
		{
			return preferenceID;
		}

		public void setPreferenceID(String property)
		{
			this.preferenceID = property;
		}

		@Override
		public boolean equals(Object obj)
		{
			if( this == obj )
			{
				return true;
			}

			if( !(obj instanceof UserPrefKey) )
			{
				return false;
			}

			UserPrefKey pkey = (UserPrefKey) obj;
			return pkey.institution == institution && userID.equals(pkey.userID)
				&& preferenceID.equals(pkey.preferenceID);
		}

		@Override
		public int hashCode()
		{
			return Check.getHashCode(institution, userID, preferenceID);
		}
	}
}
