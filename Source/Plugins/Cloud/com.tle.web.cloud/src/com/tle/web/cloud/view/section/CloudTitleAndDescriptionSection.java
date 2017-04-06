package com.tle.web.cloud.view.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.cloud.CloudConstants;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewitem.summary.section.AbstractTitleAndDescriptionSection;

/**
 * @author Aaron
 */
@NonNullByDefault
public class CloudTitleAndDescriptionSection
	extends
		AbstractTitleAndDescriptionSection<CloudItem, CloudTitleAndDescriptionSection.CloudViewItemModel>
{
	@Override
	protected CloudViewableItem getViewableItem(SectionInfo info)
	{
		return CloudItemSectionInfo.getItemInfo(info).getViewableItem();
	}

	@Override
	protected int getMaxTitleLength(SectionInfo info)
	{
		return -1;
	}

	@Override
	protected int getMaxDescriptionLength(SectionInfo info)
	{
		return -1;
	}

	@Override
	protected String getItemExtensionType()
	{
		return CloudConstants.ITEM_EXTENSION;
	}

	@Override
	public CloudViewItemModel instantiateModel(SectionInfo info)
	{
		return new CloudViewItemModel();
	}

	public static class CloudViewItemModel extends AbstractTitleAndDescriptionSection.TitleAndDescriptionModel
	{
		// Nothing specific
	}
}
