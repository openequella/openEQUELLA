package com.tle.web.viewurl.attachments;

import java.util.Collections;
import java.util.List;

import com.tle.beans.item.attachments.IAttachment;

public class DefaultAttachmentNode implements AttachmentNode
{
	private IAttachment attachment;
	private List<AttachmentNode> children;

	public DefaultAttachmentNode(IAttachment attachment)
	{
		this(attachment, null);
	}

	public DefaultAttachmentNode(IAttachment attachment, List<AttachmentNode> children)
	{
		this.attachment = attachment;
		this.children = children;
	}

	@Override
	public List<AttachmentNode> getChildren()
	{
		if( children == null )
		{
			return Collections.emptyList();
		}
		return children;
	}

	@Override
	public IAttachment getAttachment()
	{
		return attachment;
	}
}
