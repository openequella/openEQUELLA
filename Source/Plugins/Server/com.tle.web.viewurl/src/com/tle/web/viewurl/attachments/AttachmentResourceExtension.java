package com.tle.web.viewurl.attachments;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;

public interface AttachmentResourceExtension<T extends IAttachment>
{
	ViewableResource process(SectionInfo info, ViewableResource resource, T attachment);
}
