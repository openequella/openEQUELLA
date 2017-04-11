package com.tle.web.sections.render;

import com.tle.web.sections.SectionUtils;

public class ListElementState extends TagState
{
	private SectionRenderable content;

	public ListElementState(Object content)
	{
		this.content = SectionUtils.convertToRenderer(content);
	}

	public SectionRenderable getContent()
	{
		return content;
	}

	public void setContent(SectionRenderable content)
	{
		this.content = content;
	}
}
