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

package com.tle.core.favourites.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;

@Entity
@AccessType("field")
public class FavouriteSearch
{
	private static final int WITHIN_MAX = 512;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(nullable = false)
	@Index(name = "favsearchNameIndex")
	private String name;

	@Column(nullable = false)
	@Lob
	private String url;

	@Column(nullable = false)
	@Index(name = "favsearchOwnerIndex")
	private String owner;

	@Column(nullable = false)
	private Date dateModified;

	@Column(length = WITHIN_MAX)
	private String within;

	@Column(length = WITHIN_MAX)
	private String query;

	@Lob
	private String criteria;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "favsearchInstitutionIndex")
	private Institution institution;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
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

	public String getCriteria()
	{
		return criteria;
	}

	public void setCriteria(String criteria)
	{
		this.criteria = criteria;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getWithin()
	{
		return within;
	}

	public void setWithin(String within)
	{
		if( within != null && within.length() > WITHIN_MAX )
		{
			within = within.substring(0, WITHIN_MAX);
		}
		this.within = within;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		if( query != null && query.length() > WITHIN_MAX )
		{
			query = query.substring(0, WITHIN_MAX);
		}
		this.query = query;
	}

}
