package com.tle.web.viewurl.attachments;

import java.util.List;

import com.tle.beans.item.attachments.IAttachment;

public interface AttachmentNode
{
	List<AttachmentNode> getChildren();

	IAttachment getAttachment();
}
