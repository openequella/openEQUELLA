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

@NonNullByDefault
public interface ViewableItemResolverExtension
{
	<I extends IItem<?>> ViewableItem<I> createViewableItem(I item);

	<I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(I item, boolean latest,
		ViewableItemType viewableItemType);

	<I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(ItemKey itemKey, boolean latest,
		ViewableItemType viewableItemType);

	<I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem);

	<I extends IItem<?>> ViewItemUrl createViewItemUrl(SectionInfo info, ViewableItem<I> viewableItem,
		UrlEncodedString path, int flags);

	@Nullable
	<I extends IItem<?>> Bookmark createThumbnailAttachmentLink(I item, boolean latest, @Nullable String attachmentUuid);
}
