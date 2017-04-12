package com.tle.web.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.web.sections.Bookmark;

@NonNullByDefault
public interface ViewItemLinkFactory
{
	Bookmark createViewLink(ItemKey itemId);

	Bookmark createViewAttachmentLink(ItemKey itemId, @Nullable String attachmentUuid);

	Bookmark createThumbnailAttachmentLink(ItemKey itemId, @Nullable String attachmentUuid);
}
