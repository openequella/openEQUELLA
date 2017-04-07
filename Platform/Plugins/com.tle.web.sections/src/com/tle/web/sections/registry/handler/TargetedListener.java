package com.tle.web.sections.registry.handler;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionTree;

public class TargetedListener implements SectionId
{
	private final Section section;
	protected final String id;
	private final SectionTree tree;

	public TargetedListener(String id, Section section, SectionTree tree)
	{
		this.id = id;
		this.section = section;
		this.tree = tree;
	}

	@Override
	public Section getSectionObject()
	{
		return section;
	}

	@Override
	public String getSectionId()
	{
		return id;
	}

	@Override
	public SectionTree getTree()
	{
		return tree;
	}
}
