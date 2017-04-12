package com.tle.web.search.sort;

import java.util.List;

import com.tle.common.Check;
import com.tle.common.searching.Search.SortType;
import com.tle.common.searching.SortField;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.AbstractQuerySection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;

public abstract class StandardSortOptionsSection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractSortOptionsSection<SE>
{
	private static final SortOption OPTION_DATEMODIFIED = new SortOption(SortType.DATEMODIFIED);
	private static final SortOption OPTION_RANK = new SortOption(SortType.RANK);
	@TreeLookup
	private AbstractQuerySection<?, ?> querySection;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(OPTION_RANK);
		sorts.add(OPTION_DATEMODIFIED);
		sorts.add(new SortOption(SortType.DATECREATED));
		sorts.add(new SortOption(SortType.NAME));
		sorts.add(new SortOption(SortType.RATING));
	}

	@Override
	protected SortField[] createSortFromOption(SectionInfo info, SortOption selOpt)
	{
		if( selOpt.equals(OPTION_RANK) && Check.isEmpty(querySection.getParsedQuery(info)) )
		{
			selOpt = OPTION_DATEMODIFIED;
		}
		return selOpt.createSort();
	}
}
