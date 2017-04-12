package com.tle.web.viewurl;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;

@NonNullByDefault
public interface ViewItemUrlFactory
{
	/**
	 * Uses flags: FLAG_NO_BACK|FLAG_FULL_URL
	 * 
	 * @param itemId
	 * @return
	 */
	ViewItemUrl createFullItemUrl(ItemKey itemId);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId, int flags);

	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem);

	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> vieableItem, int flag);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey item, UrlEncodedString filePath);

	ViewItemUrl createItemUrl(SectionInfo info, String itemServletContext, ItemKey item, UrlEncodedString filePath,
		int flags);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey item, UrlEncodedString filePath, int flags);

	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem, UrlEncodedString filePath, int flags);
}
