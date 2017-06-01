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

package com.dytech.edge.common.valuebean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author jmaginnis
 */
public class SearchRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int SORT_DATEMODIFIED = 1;
	public static final int SORT_NAME = 2;
	public static final int SORT_FORCOUNT = 3;
	public static final int SORT_RATING = 4;
	public static final int SORT_DATECREATED = 5;

	private String query;
	private String select;
	private String where;
	private String orderby;
	private String owner;
	private boolean onlyLive = true;
	private int orderType;
	private boolean sortReverse;
	private boolean rankBoosting;
	private boolean myItem;
	private Collection<String> itemdefs;
	private Map<String, String> itemdefWheres;
	private Date[] dateRange;
	private boolean globalOnly;

	public SearchRequest()
	{
		super();
	}

	public void setMyItem(boolean myItem)
	{
		this.myItem = myItem;
	}

	public boolean isMyItem()
	{
		return myItem;
	}

	public Collection<String> getItemdefs()
	{
		return itemdefs;
	}

	public void setItemdefs(Collection<String> itemdefs)
	{
		this.itemdefs = itemdefs;
	}

	public int getOrderType()
	{
		return orderType;
	}

	public void setOrderType(int orderType)
	{
		this.orderType = orderType;
	}

	public boolean isSortReverse()
	{
		return sortReverse;
	}

	public void setSortReverse(boolean sortReverse)
	{
		this.sortReverse = sortReverse;
	}

	public boolean isOnlyLive()
	{
		return onlyLive;
	}

	public void setOnlyLive(boolean onlyLive)
	{
		this.onlyLive = onlyLive;
	}

	public String getOrderby()
	{
		return orderby;
	}

	public void setOrderby(String orderby)
	{
		this.orderby = orderby;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public String getQuery()
	{
		return query;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public boolean isRankBoosting()
	{
		return rankBoosting;
	}

	public void setRankBoosting(boolean rankBoosting)
	{
		this.rankBoosting = rankBoosting;
	}

	public String getSelect()
	{
		return select;
	}

	public void setSelect(String select)
	{
		this.select = select;
	}

	public String getWhere()
	{
		return where;
	}

	public void setWhere(String where)
	{
		this.where = where;
	}

	public Map<String, String> getItemdefWheres()
	{
		return itemdefWheres;
	}

	public void setItemdefWheres(Map<String, String> itemdefWheres)
	{
		this.itemdefWheres = itemdefWheres;
	}

	public Date[] getDateRange()
	{
		return dateRange;
	}

	public void setDateRange(Date[] dateRange)
	{
		this.dateRange = dateRange;
	}

	public boolean isGlobalOnly()
	{
		return globalOnly;
	}

	public void setGlobalOnly(boolean globalOnly)
	{
		this.globalOnly = globalOnly;
	}
}
