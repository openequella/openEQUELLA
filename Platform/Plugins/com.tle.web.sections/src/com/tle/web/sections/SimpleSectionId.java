package com.tle.web.sections;

public class SimpleSectionId implements SectionId
{
	private String id;

	public SimpleSectionId(String id)
	{
		this.id = id;
	}

	@Override
	public String getSectionId()
	{
		return id;
	}

	@Override
	public Section getSectionObject()
	{
		return null;
	}

	@Override
	public SectionTree getTree()
	{
		return null;
	}

}
