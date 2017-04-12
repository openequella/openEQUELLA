package com.tle.web.cloud.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.sections.Bookmark;

@NonNullByDefault
public interface CloudViewItemLinkFactory
{
	Bookmark createCloudViewLink(ItemKey itemId);

	Bookmark createCloudViewAttachmentLink(ItemKey itemId, String attachmentUuid);

	Bookmark createThumbnailLink(CloudItem item);

	/**
	 * @param attachment
	 * @param mimeType If you don't know, we'll work it out for you. Aren't we
	 *            nice?
	 * @return
	 */
	Bookmark createThumbnailAttachmentLink(CloudAttachment attachment, @Nullable MimeEntry mimeType);
}
