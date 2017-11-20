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

package com.tle.common;

import java.util.Collection;
import java.util.List;

/**
 * DO NOT MOVE THE PACKAGE LOCATION
 * 
 * @author Nicholas Read
 */
public class SavedSearch
{
	private String name;
	private int type;
	private String query;
	@Deprecated
	private String insideQuery;
	@Deprecated
	private String insideDisplay;
	private Collection<String> collections;
	@Deprecated
	private Collection<Long> itemdefs;
	@Deprecated
	private long powersearch;
	private String powersearchUuid;
	private String powersearchxml;
	@Deprecated
	private boolean synonym;
	private String sort;
	private String hierarchyTopicUuid;

	private List<String> insideQueries;
	private List<Boolean> insideSyn;

	public SavedSearch()
	{
		super();
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Deprecated
	public Collection<Long> getItemdefs()
	{
		return itemdefs;
	}

	@Deprecated
	public void setItemdefs(Collection<Long> itemdefs)
	{
		this.itemdefs = itemdefs;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	@Deprecated
	public boolean isSynonym()
	{
		return synonym;
	}

	@Deprecated
	public void setSynonym(boolean synonym)
	{
		this.synonym = synonym;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	@Deprecated
	public String getInsideDisplay()
	{
		return insideDisplay;
	}

	@Deprecated
	public void setInsideDisplay(String insideDisplay)
	{
		this.insideDisplay = insideDisplay;
	}

	@Deprecated
	public String getInsideQuery()
	{
		return insideQuery;
	}

	@Deprecated
	public void setInsideQuery(String insideQuery)
	{
		this.insideQuery = insideQuery;
	}

	@Deprecated
	public long getPowersearch()
	{
		return powersearch;
	}

	@Deprecated
	public void setPowersearch(long powersearch)
	{
		this.powersearch = powersearch;
	}

	public String getPowersearchxml()
	{
		return powersearchxml;
	}

	public void setPowersearchxml(String powersearchxml)
	{
		this.powersearchxml = powersearchxml;
	}

	public String getPowersearchUuid()
	{
		return powersearchUuid;
	}

	public void setPowersearchUuid(String powersearchUuid)
	{
		this.powersearchUuid = powersearchUuid;
	}

	public String getSort()
	{
		return sort;
	}

	public void setSort(String sort)
	{
		this.sort = sort;
	}

	public String getHierarchyTopicUuid()
	{
		return hierarchyTopicUuid;
	}

	public void setHierarchyTopicUuid(String hierarchyTopicUuid)
	{
		this.hierarchyTopicUuid = hierarchyTopicUuid;
	}

	public List<String> getInsideQueries()
	{
		return insideQueries;
	}

	public void setInsideQueries(List<String> insideQueries)
	{
		this.insideQueries = insideQueries;
	}

	public List<Boolean> getInsideSyn()
	{
		return insideSyn;
	}

	public void setInsideSyn(List<Boolean> insideSyn)
	{
		this.insideSyn = insideSyn;
	}

	public Collection<String> getCollections()
	{
		return collections;
	}

	public void setCollections(Collection<String> collections)
	{
		this.collections = collections;
	}
}
