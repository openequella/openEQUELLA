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

package com.tle.core.replicatedcache.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;

import com.dytech.devlib.Base64;
import com.tle.beans.Institution;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"institution_id", "cacheId", "key"})})
public class CachedValue
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@OneToOne(fetch = FetchType.LAZY)
	private Institution institution;

	private String cacheId;
	private String key;
	@Lob
	private String value;
	private Date ttl;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getCacheId()
	{
		return cacheId;
	}

	public void setCacheId(String cacheId)
	{
		this.cacheId = cacheId;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public byte[] getValue()
	{
		return new Base64().decode(value);
	}

	public void setValue(byte[] value)
	{
		this.value = new Base64().encode(value);
	}

	public Date getTtl()
	{
		return ttl;
	}

	public void setTtl(Date ttl)
	{
		this.ttl = ttl;
	}
}
