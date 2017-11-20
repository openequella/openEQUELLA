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

package com.tle.core.connectors.brightspace.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public abstract class AbstractPagedResults<T>
{
	@JsonProperty("PagingInfo")
	private PagingInfo pagingInfo;
	@JsonProperty("Items")
	private List<T> items;

	public PagingInfo getPagingInfo()
	{
		return pagingInfo;
	}

	public void setPagingInfo(PagingInfo pagingInfo)
	{
		this.pagingInfo = pagingInfo;
	}

	public List<T> getItems()
	{
		return items;
	}

	public void setItems(List<T> items)
	{
		this.items = items;
	}

	public static class PagingInfo
	{
		@JsonProperty("Bookmark")
		private String bookmark;
		@JsonProperty("HasMoreItems")
		private boolean hasMoreItems;

		public String getBookmark()
		{
			return bookmark;
		}

		public void setBookmark(String bookmark)
		{
			this.bookmark = bookmark;
		}

		public boolean isHasMoreItems()
		{
			return hasMoreItems;
		}

		public void setHasMoreItems(boolean hasMoreItems)
		{
			this.hasMoreItems = hasMoreItems;
		}
	}
}
