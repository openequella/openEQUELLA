package com.tle.web.selection;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface SelectableAttachment
{
	/**
	 * 
	 * @param info
	 * @param item
	 * @param attachmentUuid null in the case of determining if <em>any</em> attachment is selectable
	 * @return
	 */
	boolean isAttachmentSelectable(SectionInfo info, IItem<?> item, @Nullable String attachmentUuid);

	boolean canBePushed(String attachmentUuid);

	boolean isItemCopyrighted(IItem<?> item);

	List<String> getApplicableCourseCodes(String attachmentUuid);
}
