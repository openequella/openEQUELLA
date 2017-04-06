package com.tle.web.search.filter;

import java.util.Date;

import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;

public class FilterByDateRangeSection extends AbstractFilterByDateRangeSection<FreetextSearchEvent>
{
	@PlugKey("filter.bydate.title")
	private static Label LABEL_TITLE;

	@SuppressWarnings("nls")
	@Override
	protected String[] getParameterNames()
	{
		return new String[]{"dp", "ds", "dr"};
	}

	@Override
	public String getAjaxDiv()
	{
		return "date-range-filter"; //$NON-NLS-1$
	}

	@Override
	public Label getTitle()
	{
		return LABEL_TITLE;
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		Date[] dateRange = getDateRange(info);
		if( dateRange != null )
		{
			event.filterByDateRange(dateRange);
		}
	}

}
