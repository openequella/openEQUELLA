package com.tle.web.selection;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface SelectAttachmentHandler
{
	void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType);
}
