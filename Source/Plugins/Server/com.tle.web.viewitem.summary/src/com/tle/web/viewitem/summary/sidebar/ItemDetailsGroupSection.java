package com.tle.web.viewitem.summary.sidebar;

import com.tle.core.guice.Bind;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.viewitem.summary.section.AbstractItemDetailsSection;

@SuppressWarnings("nls")
@Bind
public class ItemDetailsGroupSection extends AbstractItemDetailsSection<AbstractItemDetailsSection.ItemDetailsModel>
{
	@Override
	protected String getTemplate(RenderEventContext context)
	{
		return "viewitem/summary/sidebar/itemdetailsgroup.ftl";
	}
}
