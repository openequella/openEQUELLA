package com.tle.web.myresources;

import com.tle.web.sections.SectionInfo;

public class AllResourcesSubSearch extends AbstractMyResourcesSubSearch
{

	public AllResourcesSubSearch(String nameKey)
	{
		super(nameKey, "all", 1000); //$NON-NLS-1$
	}

	@Override
	public MyResourcesSearch createDefaultSearch(SectionInfo info)
	{
		return new MyResourcesSearch();
	}

	@Override
	public void setupFilters(SectionInfo info)
	{
		// Use default filters
	}

}
