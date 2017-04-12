package com.tle.web.viewurl.attachments;

import java.net.URI;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.impl.AttachmentResourceServiceImpl.PathViewableResource;

@NonNullByDefault
public interface AttachmentResourceService
{
	ViewableResource getViewableResource(SectionInfo info, ViewableItem viewableItem, IAttachment attachment);

	/**
	 * @param info
	 * @param viewableItem
	 * @param path
	 * @param attachment May be null. Used to extract the associated viewer
	 * @return
	 */
	PathViewableResource createPathResource(SectionInfo info, ViewableItem viewableItem, String path,
		@Nullable IAttachment attachment);

	/**
	 * @param info
	 * @param viewableItem
	 * @param path
	 * @param description
	 * @param mimeType
	 * @param attachment May be null. Used to extract the associated viewer
	 * @return
	 */
	PathViewableResource createPathResource(SectionInfo info, ViewableItem viewableItem, String path,
		String description, String mimeType, @Nullable IAttachment attachment);

	URI getPackageZipFileUrl(Item item, Attachment attachment);
}
