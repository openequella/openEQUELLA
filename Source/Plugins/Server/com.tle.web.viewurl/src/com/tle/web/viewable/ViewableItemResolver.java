package com.tle.web.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface ViewableItemResolver
{
	<I extends IItem<?>> ViewableItem<I> createViewableItem(I item, @Nullable String extensionType);

	<I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(I item, boolean latest,
		ViewableItemType viewableItemType, @Nullable String extensionType);

	<I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(ItemKey itemKey, boolean latest,
		ViewableItemType viewableItemType, @Nullable String extensionType);

	<I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		@Nullable String extensionType);

	<I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		UrlEncodedString path, int flags, @Nullable String extensionType);

	@Nullable
	<I extends IItem<?>> Bookmark createThumbnailAttachmentLink(I item, boolean latest,
		@Nullable String attachmentUuid, @Nullable String extensionType);
}
