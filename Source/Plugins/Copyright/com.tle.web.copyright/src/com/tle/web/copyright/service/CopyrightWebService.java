package com.tle.web.copyright.service;

import java.util.Map;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.copyright.Holding;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface CopyrightWebService<H extends Holding>
{
	Map<String, Attachment> getAttachmentMap(SectionInfo info, Item item);

	int getStatus(SectionInfo info, Item item, String attachmentUuid);

	H getHolding(SectionInfo info, Item item);

	String getAgreement(FileHandle agreementFile);
}
