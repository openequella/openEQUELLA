package com.tle.web.sections.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

@NonNullByDefault
public class DefaultSectionContext extends WrappedSectionInfo implements SectionContext
{
	private final String id;
	private final Section section;
	private final SectionTree tree;

	public DefaultSectionContext(Section section, MutableSectionInfo info, SectionTree tree, String id)
	{
		super(info);
		this.id = id;
		this.section = section;
		this.tree = tree;
		this.info = info;
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

	public String getId()
	{
		return id;
	}

	@Override
	public SectionInfo getInfo()
	{
		return info;
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getModel()
	{
		return (T) info.getModelForId(id);
	}

	@Override
	public Section getSectionObject()
	{
		return section;
	}

	@Override
	public Section getSection()
	{
		return section;
	}
}
