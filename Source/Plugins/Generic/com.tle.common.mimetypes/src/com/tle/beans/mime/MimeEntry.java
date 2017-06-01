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

package com.tle.beans.mime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.Type;

import com.tle.beans.Institution;

@Entity
@AccessType("field")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"type", "institution_id"}))
@NamedQueries({
		@NamedQuery(name = "searchMimeTypes", cacheable = true, readOnly = true, query = "FROM MimeEntry m WHERE m.type LIKE :query AND m.institution = :institution ORDER BY m.type"),
		@NamedQuery(name = "countMimeTypes", cacheable = true, readOnly = true, query = "SELECT COUNT(*) FROM MimeEntry m WHERE m.type LIKE :query AND m.institution = :institution")})
public class MimeEntry
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "mimeInstitutionIndex")
	private Institution institution;

	@ElementCollection
	@Column(name = "element", length = 20)
	private Collection<String> extensions = new ArrayList<String>();

	@Column(length = 100, nullable = false)
	private String type;

	@Column(length = 512)
	private String description;

	@ElementCollection
	@Column(name = "element", nullable = false)
	@Lob
	@MapKeyColumn(name = "mapkey", length = 100, nullable = false)
	@MapKeyType(@Type(type = "string"))
	private Map<String, String> attributes = new HashMap<String, String>();

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Collection<String> getExtensions()
	{
		return extensions;
	}

	public void setExtensions(Collection<String> extensions)
	{
		this.extensions = extensions;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public Map<String, String> getAttributes()
	{
		return attributes;
	}

	public String getAttribute(String key)
	{
		return attributes.get(key);
	}

	public void setAttributes(Map<String, String> attributes)
	{
		this.attributes = attributes;
	}

	public void setAttribute(String key, String value)
	{
		this.attributes.put(key, value);
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

}
