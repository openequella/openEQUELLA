package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.SectionRenderable;

public class TabContent
{
	private final String name;
	private final String value;
	private final int index;
	private final SectionRenderable renderer;

	public TabContent(int index, String name, String value, SectionRenderable renderer)
	{
		this.index = index;
		this.name = name;
		this.value = value;
		this.renderer = renderer;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

	public SectionRenderable getRenderer()
	{
		return renderer;
	}

	public int getIndex()
	{
		return index;
	}

}
