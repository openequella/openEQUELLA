package com.tle.web.selection;

import java.util.List;

import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;

public interface SelectableAttachment
{
	boolean isAttachmentSelectable(SectionInfo info, IItem<?> item, String attachmentUuid);

	boolean canBePushed(String attachmentUuid);

	boolean isItemCopyrighted(IItem<?> item);

	List<String> getApplicableCourseCodes(String attachmentUuid);
}
