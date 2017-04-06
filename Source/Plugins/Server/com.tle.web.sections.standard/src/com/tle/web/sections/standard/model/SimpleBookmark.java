package com.tle.web.sections.standard.model;

import com.tle.web.sections.Bookmark;

public class SimpleBookmark implements Bookmark
{
	private String href;

	public SimpleBookmark(String href)
	{
		this.href = href;
	}

	@Override
	public String getHref()
	{
		return href;
	}

}
