package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.beans.item.Item;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;

public class UnselectItemSummarySection extends AbstractUnselectItemSummarySection<Item, Object>
{
	@Override
	protected ViewableItem<Item> getViewableItem(SectionInfo info)
	{
		return ParentViewItemSectionUtils.getItemInfo(info).getViewableItem();
	}

	@Override
	protected String getItemExtensionType()
	{
		return null;
	}
}
