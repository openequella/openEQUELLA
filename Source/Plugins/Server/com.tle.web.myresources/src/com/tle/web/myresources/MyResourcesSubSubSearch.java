package com.tle.web.myresources;

import com.tle.common.search.DefaultSearch;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;

public abstract class MyResourcesSubSubSearch
{
	private final Label name;
	private final DefaultSearch search;

	public MyResourcesSubSubSearch(Label name, DefaultSearch search)
	{
		this.name = name;
		this.search = search;
	}

	public abstract void execute(SectionInfo info);

	public Label getName()
	{
		return name;
	}

	public DefaultSearch getSearch()
	{
		return search;
	}
}
