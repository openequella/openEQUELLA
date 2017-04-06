package com.tle.web.viewurl.attachments;

import java.util.List;

import com.tle.beans.item.attachments.IAttachment;

public interface AttachmentTreeExtension
{
	/**
	 * Create AttachmentNode's for an attachment.
	 * 
	 * @param attachment
	 * @param otherAttachments
	 * @param rootNodes
	 * @param flattenHidden
	 */
	void addRootAttachmentNode(IAttachment attachment, List<IAttachment> otherAttachments,
		List<AttachmentNode> rootNodes, boolean flattenHidden);
}
