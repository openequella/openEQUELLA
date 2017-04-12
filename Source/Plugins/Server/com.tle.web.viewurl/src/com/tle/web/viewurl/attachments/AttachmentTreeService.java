package com.tle.web.viewurl.attachments;

import java.util.List;

import com.tle.beans.item.attachments.IAttachment;

public interface AttachmentTreeService
{
	List<AttachmentNode> getTreeStructure(Iterable<? extends IAttachment> attachments, boolean flattenHiddenChildren);
}
