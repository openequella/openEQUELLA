package com.tle.web.payment.shop.section.search;

import java.util.List;

import com.tle.web.payment.shop.event.ShopSearchEvent;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

/**
 * @author dustin
 */
@SuppressWarnings("nls")
public class ShopSearchSortSection extends AbstractSortOptionsSection<ShopSearchEvent>
{
	// These values are passed to the REST query
	private static final String VAL_RELEVANCE = "relevance";
	private static final String VAL_MODIFIED = "modified";
	private static final String VAL_TITLE = "name";

	@PlugKey("store.search.sort.relevance")
	private static Label RELEVANCE_LABEL;
	@PlugKey("store.search.sort.modified")
	private static Label MODIFIED_LABEL;
	@PlugKey("store.search.sort.title")
	private static Label TITLE_LABEL;

	@Override
	protected void addSortOptions(List<SortOption> sorts)
	{
		sorts.add(new SortOption(RELEVANCE_LABEL, VAL_RELEVANCE, null));
		sorts.add(new SortOption(MODIFIED_LABEL, VAL_MODIFIED, null));
		sorts.add(new SortOption(TITLE_LABEL, VAL_TITLE, null));
	}

	@Override
	public void prepareSearch(SectionInfo info, ShopSearchEvent event)
	{
		event.setSort(sortOptions.getSelectedValueAsString(info));
		event.setReverse(reverse.isChecked(info));
	}

	@Override
	protected String getDefaultSearch(SectionInfo info)
	{
		return VAL_RELEVANCE;
	}
}
