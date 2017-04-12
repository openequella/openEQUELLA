package com.tle.web.cloud.search.section;

import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;

@SuppressWarnings("nls")
public class CloudQuerySection extends SimpleResetFiltersQuerySection<CloudSearchEvent>
{
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		renderQueryActions(context, getModel(context));
		return viewFactory.createResult("cloudquery.ftl", this);
	}
}
