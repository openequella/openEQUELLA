package com.tle.web.cloud.view.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemKey;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

/**
 * @author Aaron
 */
@NonNullByDefault
public class CloudItemSectionInfo
{
	private final CloudViewableItem viewableItem;

	public CloudItemSectionInfo(CloudViewableItem viewableItem)
	{
		this.viewableItem = viewableItem;
	}

	public CloudViewableItem getViewableItem()
	{
		return viewableItem;
	}

	public ItemKey getItemId()
	{
		return viewableItem.getItemId();
	}

	public static CloudItemSectionInfo getItemInfo(SectionInfo info)
	{
		CloudItemSectionInfo iinfo = info.getAttribute(CloudItemSectionInfo.class);
		if( iinfo == null )
		{
			CloudItemSectionInfoFactory factory = info.lookupSection(CloudItemSectionInfoFactory.class);
			iinfo = factory.createCloudItemSectionInfo(info);
			info.setAttribute(CloudItemSectionInfo.class, iinfo);
		}
		return iinfo;
	}

	@TreeIndexed
	public interface CloudItemSectionInfoFactory extends SectionId
	{
		CloudItemSectionInfo createCloudItemSectionInfo(SectionInfo info);
	}
}
