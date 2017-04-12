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
