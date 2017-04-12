package com.tle.web.favourites.sort;

import java.util.List;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.searching.SortField;
import com.tle.core.user.CurrentUser;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.sort.SortOption;
import com.tle.web.search.sort.StandardSortOptionsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class FavouritesSortOptionsSection extends StandardSortOptionsSection<FreetextSearchEvent>
{
	private static final String FAV_DATE_VALUE = "favdate";

	@PlugKey("sortoptions.items.bookmarkdate")
	private static Label LABEL_BOOKDATE;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(LABEL_BOOKDATE, FAV_DATE_VALUE, null)
		{
			@Override
			public SortField[] createSort()
			{
				return new SortField[]{new SortField(FreeTextQuery.FIELD_BOOKMARK_DATE + CurrentUser.getUserID(), true)};
			}
		});
		super.addSortOptions(sorts);
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return FAV_DATE_VALUE;
	}
}
