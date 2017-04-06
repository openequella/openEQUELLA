package com.tle.web.viewurl;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

public class ViewAttachmentUrl implements ItemUrlExtender
{
	private static final long serialVersionUID = 1L;

	private final String uuid;

	public ViewAttachmentUrl(String uuid)
	{
		this.uuid = uuid;
	}

	@Override
	public void execute(SectionInfo info)
	{
		ViewAttachmentInterface section = info.lookupSection(ViewAttachmentInterface.class);
		section.setAttachmentToView(info, uuid);
	}

	@TreeIndexed
	public interface ViewAttachmentInterface extends SectionId
	{
		void setAttachmentToView(SectionInfo info, String attachmentUuid);
	}
}