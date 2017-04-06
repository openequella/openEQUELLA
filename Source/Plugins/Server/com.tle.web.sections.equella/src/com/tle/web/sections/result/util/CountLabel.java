package com.tle.web.sections.result.util;

import com.tle.web.sections.render.Label;

public class CountLabel implements Label
{
	private long count;

	public CountLabel(long count)
	{
		this.count = count;
	}

	@SuppressWarnings("nls")
	@Override
	public String getText()
	{
		String extraClass = count == 0 ? " class=\"zero\"" : "";
		return "<span" + extraClass + ">" + count + "</span>";
	}

	@Override
	public boolean isHtml()
	{
		return true;
	}

}
