package com.tle.web.viewitem.section;

import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ItemSectionInfo.ItemSectionInfoFactory;

public final class ParentViewItemSectionUtils
{
	public static ItemSectionInfo getItemInfo(SectionInfo info)
	{
		ItemSectionInfo iinfo = info.getAttribute(ItemSectionInfo.class);
		if( iinfo == null )
		{
			ItemSectionInfoFactory factory = info.lookupSection(ItemSectionInfoFactory.class);
			iinfo = factory.getItemSectionInfo(info);
			info.setAttribute(ItemSectionInfo.class, iinfo);
		}
		return iinfo;
	}

	public static boolean isForPreview(SectionInfo info)
	{
		RootItemFileSection section = info.lookupSection(RootItemFileSection.class);
		return section.isForPreview(info);
	}

	public static boolean isInIntegration(SectionInfo info)
	{
		RootItemFileSection section = info.lookupSection(RootItemFileSection.class);
		return section.isInIntegration(info);
	}

	private ParentViewItemSectionUtils()
	{
		throw new Error();
	}
}
