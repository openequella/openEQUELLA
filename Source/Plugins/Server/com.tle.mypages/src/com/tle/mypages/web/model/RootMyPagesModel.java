package com.tle.mypages.web.model;

import java.util.List;

import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
public class RootMyPagesModel
{
	protected List<SectionRenderable> sections;

	public List<SectionRenderable> getSections()
	{
		return sections;
	}

	public void setSections(List<SectionRenderable> sections)
	{
		this.sections = sections;
	}

}
