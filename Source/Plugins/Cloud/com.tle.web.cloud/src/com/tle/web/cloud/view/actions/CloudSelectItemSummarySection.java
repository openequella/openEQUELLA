package com.tle.web.cloud.view.actions;

import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.cloud.view.section.CloudItemSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.summary.sidebar.actions.AbstractSelectItemSummarySection;

/**
 * @author Aaron
 */
public class CloudSelectItemSummarySection extends AbstractSelectItemSummarySection<CloudItem, Object>
{
	@Override
	protected ViewableItem<CloudItem> getViewableItem(SectionInfo info)
	{
		return CloudItemSectionInfo.getItemInfo(info).getViewableItem();
	}

	@Override
	protected String getItemExtensionType()
	{
		return CloudConstants.ITEM_EXTENSION;
	}
}
