package com.tle.web.viewitem;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewitem.attachments.AttachmentView;

@NonNullByDefault
public interface AttachmentViewFilter
{
	boolean shouldBeDisplayed(SectionInfo info, AttachmentView attachmentView);
}
