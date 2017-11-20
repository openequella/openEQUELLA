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
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

@Entity
@AccessType("field")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"urlHash"}))
public class ReferencedURL implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int MAX_MESSAGE_LENGTH = 250;

	public static final Predicate<ReferencedURL> BAD_URLS = new Predicate<ReferencedURL>()
	{
		@Override
		public boolean apply(ReferencedURL input)
		{
			return !input.isSuccess();
		}
	};

	public static Collection<ReferencedURL> keepBadUrls(Collection<ReferencedURL> urls)
	{
		return Collections2.filter(urls, ReferencedURL.BAD_URLS);
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	// Cannot be Indexed due to SQL Server being a fail whale
	@Lob
	private String url;

	// Computed MD5 hash of url so SQL Server and Oracle don't cry
	@Column(length = 32)
	@Index(name = "referencedurl_idx")
	private String urlHash;

	@NotNull
	private boolean success;
	@NotNull
	private int status;
	private String message;
	@NotNull
	private int tries = 0;

	@NotNull
	private Date lastChecked;
	@NotNull
	private Date lastIndexed;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
		this.urlHash = DigestUtils.md5Hex(url);
	}

	public String getUrlHash()
	{
		return urlHash;
	}

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public int getStatus()
	{
		return status;
	}

	public void setStatus(int status)
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		if( message != null && message.length() > MAX_MESSAGE_LENGTH )
		{
			message = message.substring(0, MAX_MESSAGE_LENGTH);
		}
		this.message = message;
	}

	public int getTries()
	{
		return tries;
	}

	public void setTries(int tries)
	{
		this.tries = tries;
	}

	public Date getLastChecked()
	{
		return lastChecked;
	}

	public void setLastChecked(Date lastChecked)
	{
		this.lastChecked = lastChecked;
	}

	public Date getLastIndexed()
	{
		return lastIndexed;
	}

	public void setLastIndexed(Date lastIndexed)
	{
		this.lastIndexed = lastIndexed;
	}

	@Override
	public String toString()
	{
		return url;
	}

	@Override
	public int hashCode()
	{
		return url.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof ReferencedURL) )
		{
			return false;
		}

		return url.equals(((ReferencedURL) obj).url);
	}
}
