package com.tle.web.sections.generic;

import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionTree;

public abstract class AbstractSectionFilter implements SectionFilter
{
	@Override
	public void filter(final MutableSectionInfo minfo)
	{
		final SectionTree filterTree = getFilterTree();
		minfo.addTree(filterTree);
	}

	protected abstract SectionTree getFilterTree();

}
