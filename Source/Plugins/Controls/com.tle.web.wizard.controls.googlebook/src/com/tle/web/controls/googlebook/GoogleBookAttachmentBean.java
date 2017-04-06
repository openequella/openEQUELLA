package com.tle.web.controls.googlebook;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class GoogleBookAttachmentBean extends EquellaAttachmentBean
{
	private String bookId;
	private String viewUrl;
	private String thumbUrl;
	private String publishedDate;
	private String pages;

	public String getBookId()
	{
		return bookId;
	}

	public void setBookId(String bookId)
	{
		this.bookId = bookId;
	}

	public String getViewUrl()
	{
		return viewUrl;
	}

	public void setViewUrl(String viewUrl)
	{
		this.viewUrl = viewUrl;
	}

	public String getThumbUrl()
	{
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl)
	{
		this.thumbUrl = thumbUrl;
	}

	public String getPublishedDate()
	{
		return publishedDate;
	}

	public void setPublishedDate(String publishedDate)
	{
		this.publishedDate = publishedDate;
	}

	public String getPages()
	{
		return pages;
	}

	public void setPages(String pages)
	{
		this.pages = pages;
	}

	@Override
	public String getRawAttachmentType()
	{
		return "custom/googlebook";
	}
}
