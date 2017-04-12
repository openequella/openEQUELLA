package com.tle.web.wizard.page;

import com.tle.web.sections.render.TagRenderer;

public class ControlResult
{
	private String sectionId;
	private TagRenderer result;

	public ControlResult(String sectionId, TagRenderer renderer)
	{
		this.sectionId = sectionId;
		this.result = renderer;
	}

	public String getSectionId()
	{
		return sectionId;
	}

	public void setSectionId(String sectionId)
	{
		this.sectionId = sectionId;
	}

	public TagRenderer getResult()
	{
		return result;
	}

	public void setResult(TagRenderer result)
	{
		this.result = result;
	}

}
