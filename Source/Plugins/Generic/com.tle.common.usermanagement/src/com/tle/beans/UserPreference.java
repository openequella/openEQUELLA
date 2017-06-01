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
