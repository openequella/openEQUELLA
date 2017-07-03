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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Entity
@AccessType("field")
public class Institution implements Serializable
{
	private static final long serialVersionUID = -55542832531237914L;

	public static final Institution FAKE = new Institution();
	static
	{
		FAKE.setUniqueId(-1);
	}

	/**
	 * This is the ID used in the database. It is **NOT** necessarily unique for
	 * institutions across different schemas.
	 */
	@Id
	@GeneratedValue
	private long id;

	/**
	 * This ID is guaranteed to be unique across schemas and is safe for use in
	 * maps, sets, etc... It may be changed when an institution is taken
	 * online/offline.
	 */
	@Column(nullable = false)
	private long uniqueId;
	@Column(length = 100)
	private double quota;
	@Column(length = 100, nullable = false)
	private String name;
	@Column(name = "shortName", unique = true, length = 20, nullable = false)
	private String filestoreId;
	@Column(unique = true, length = 100)
	private String url;
	@Column(nullable = false)
	private String adminPassword;
	@Column(length = 20)
	private String timeZone;

	private boolean enabled = true;

	/**
	 * Can't remove this because it is XStreamed in the root of institution
	 * exports -> institutionData.xml
	 */
	@Transient
	@Deprecated
	@XStreamOmitField
	@SuppressWarnings("unused")
	private String badgeUrl;

	public Institution()
	{
		// nothing
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getFilestoreId()
	{
		return filestoreId;
	}

	public void setFilestoreId(String filestoreId)
	{
		this.filestoreId = filestoreId;
	}

	public String getUrl()
	{
		return url;
	}

	public URL getUrlAsUrl()
	{
		try
		{
			return new URL(getUrl());
		}
		catch( MalformedURLException ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public URI getUrlAsUri()
	{
		return URI.create(getUrl());
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public long getDatabaseId()
	{
		return id;
	}

	public void setDatabaseId(long id)
	{
		this.id = id;
	}

	public long getUniqueId()
	{
		return uniqueId;
	}

	public void setUniqueId(long uniqueId)
	{
		this.uniqueId = uniqueId;
	}

	public double getQuota()
	{
		return quota;
	}

	public void setQuota(double quota)
	{
		this.quota = quota;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof Institution) )
		{
			return false;
		}

		return uniqueId == ((Institution) obj).uniqueId;
	}

	@Override
	public int hashCode()
	{
		return Long.valueOf(uniqueId).hashCode();
	}

	public String getAdminPassword()
	{
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword)
	{
		this.adminPassword = adminPassword;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public String getTimeZone()
	{
		return timeZone;
	}

	public void setTimeZone(String timeZone)
	{
		this.timeZone = timeZone;
	}
}
