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

package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import com.tle.beans.IdCloneable;

@Entity
@AccessType("field")
public class ItemdefBlobs implements IdCloneable, Serializable
{
	private static final long serialVersionUID = -12L;

	@Id
	// This needs to be generated for MySQL
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long id;

	@Type(type = "xstream_immutable")
	private List<ItemMetadataRule> itemMetadataRules;
	@Type(type = "xstream_immutable")
	private List<DynamicMetadataRule> dynamicMetadataRules;
	@Type(type = "xstream_immutable")
	private Wizard wizard;
	@Type(type = "xstream_immutable")
	private SearchDetails searchDetails;
	@Type(type = "xstream_immutable")
	private MetadataMapping metadataMapping;
	@Type(type = "xstream_immutable")
	private SummaryDisplayTemplate itemSummarySections;

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public List<ItemMetadataRule> getItemMetadataRules()
	{
		return itemMetadataRules;
	}

	public void setItemMetadataRules(List<ItemMetadataRule> itemMetadataRules)
	{
		this.itemMetadataRules = itemMetadataRules;
	}

	public List<DynamicMetadataRule> getDynamicMetadataRules()
	{
		return dynamicMetadataRules;
	}

	public void setDynamicMetadataRules(List<DynamicMetadataRule> dynamicMetadataRules)
	{
		this.dynamicMetadataRules = dynamicMetadataRules;
	}

	public MetadataMapping getMetadataMapping()
	{
		return metadataMapping;
	}

	public void setMetadataMapping(MetadataMapping metadataMapping)
	{
		this.metadataMapping = metadataMapping;
	}

	public SearchDetails getSearchDetails()
	{
		return searchDetails;
	}

	public void setSearchDetails(SearchDetails searchDetails)
	{
		this.searchDetails = searchDetails;
	}

	public Wizard getWizard()
	{
		return wizard;
	}

	public void setWizard(Wizard wizard)
	{
		this.wizard = wizard;
	}

	public void setItemSummarySections(SummaryDisplayTemplate itemSummarySections)
	{
		this.itemSummarySections = itemSummarySections;
	}

	public SummaryDisplayTemplate getItemSummarySections()
	{
		return itemSummarySections;
	}
}
