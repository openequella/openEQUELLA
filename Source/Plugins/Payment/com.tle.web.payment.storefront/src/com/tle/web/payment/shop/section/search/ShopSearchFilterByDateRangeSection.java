package com.tle.web.payment.shop.section.search;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.web.payment.shop.event.ShopSearchEvent;
import com.tle.web.search.filter.AbstractFilterByDateRangeSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.render.Label;

@NonNullByDefault
public class ShopSearchFilterByDateRangeSection extends AbstractFilterByDateRangeSection<ShopSearchEvent>
{
	@PlugKey("store.search.filter.date")
	private static Label LABEL_TITLE;

	private String layout = SearchResultsActionsSection.AREA_FILTER;

	@Override
	public void registered(String id, SectionTree tree)
	{
		if( !Check.isEmpty(layout) )
		{
			tree.setLayout(id, layout);
		}
		super.registered(id, tree);
	}

	@Override
	public void prepareSearch(SectionInfo info, ShopSearchEvent event) throws Exception
	{
		event.setDaterange(getDateRange(info));
	}

	@Override
	protected String[] getParameterNames()
	{
		return new String[]{"dp", "ds", "dr"};
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public String getAjaxDiv()
	{
		return "date-range-filter"; //$NON-NLS-1$
	}
}
