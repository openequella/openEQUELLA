package com.tle.mycontent.web.sort;

import java.util.List;

import com.tle.common.searching.Search.SortType;
import com.tle.core.guice.Bind;
import com.tle.web.myresources.MyResourcesSortSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;

@SuppressWarnings("nls")
@Bind
public class ScrapbookSortSection extends AbstractSortOptionsSection<FreetextSearchEvent>
{
	@TreeLookup
	private MyResourcesSortSection otherSort;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		sortOptions.setParameterId("sbsort");
		reverse.setParameterId("sbrs");
	}

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(SortType.DATEMODIFIED));
		sorts.add(new SortOption(SortType.DATECREATED));
		sorts.add(new SortOption(SortType.NAME));
	}

	public void enable(SectionInfo info)
	{
		getModel(info).setDisabled(false);
		otherSort.disable(info);
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return "datemodified";
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		SortOptionsModel model = new SortOptionsModel();
		model.setDisabled(true);
		return model;
	}
}
