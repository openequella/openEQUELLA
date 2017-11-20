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

package com.tle.beans.entity;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.tle.beans.ItemDefinitionScript;
import com.tle.beans.SchemaScript;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
public class DynaCollection extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@Column(name = "element")
	private Set<String> usageIds;

	@Column(length = 100)
	private String freetextQuery;

	@ElementCollection(fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<ItemDefinitionScript> itemDefs;

	@ElementCollection(fetch = FetchType.LAZY)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<SchemaScript> schemas;

	@Column(length = 100)
	private String virtualisationId;

	@Column(length = 255)
	private String virtualisationPath;

	public DynaCollection()
	{
		super();
	}

	public DynaCollection(long id)
	{
		setId(id);
	}

	public Set<String> getUsageIds()
	{
		return usageIds;
	}

	public void setUsageIds(Set<String> usageIds)
	{
		this.usageIds = usageIds;
	}

	public String getFreetextQuery()
	{
		return freetextQuery;
	}

	public void setFreetextQuery(String freetextQuery)
	{
		this.freetextQuery = freetextQuery;
	}

	public List<ItemDefinitionScript> getItemDefs()
	{
		return itemDefs;
	}

	public void setItemDefs(List<ItemDefinitionScript> itemDefs)
	{
		this.itemDefs = itemDefs;
	}

	public List<SchemaScript> getSchemas()
	{
		return schemas;
	}

	public void setSchemas(List<SchemaScript> schemas)
	{
		this.schemas = schemas;
	}

	public String getVirtualisationId()
	{
		return virtualisationId;
	}

	public void setVirtualisationId(String virtualisationId)
	{
		this.virtualisationId = virtualisationId;
	}

	public String getVirtualisationPath()
	{
		return virtualisationPath;
	}

	public void setVirtualisationPath(String virtualisationPath)
	{
		this.virtualisationPath = virtualisationPath;
	}
}
