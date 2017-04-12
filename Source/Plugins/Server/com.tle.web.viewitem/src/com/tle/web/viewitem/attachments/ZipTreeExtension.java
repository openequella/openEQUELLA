package com.tle.web.viewitem.attachments;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.core.guice.Bind;
import com.tle.web.viewurl.attachments.AttachmentNode;
import com.tle.web.viewurl.attachments.AttachmentTreeExtension;
import com.tle.web.viewurl.attachments.DefaultAttachmentNode;

@Bind
@Singleton
public class ZipTreeExtension implements AttachmentTreeExtension
{

	@Override
	public void addRootAttachmentNode(IAttachment attachment, List<IAttachment> otherAttachments,
		List<AttachmentNode> rootNodes, boolean flattenHidden)
	{
		ZipAttachment zipAttachment = (ZipAttachment) attachment;
		String uuid = zipAttachment.getUuid();
		Iterator<AttachmentNode> iter = rootNodes.iterator();
		List<AttachmentNode> children = Lists.newArrayList();
		while( iter.hasNext() )
		{
			AttachmentNode processedNode = iter.next();
			String zipUuid = (String) processedNode.getAttachment().getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID);
			if( uuid.equals(zipUuid) )
			{
				children.add(processedNode);
				iter.remove();
			}
		}
		Iterator<IAttachment> aiter = otherAttachments.iterator();
		while( aiter.hasNext() )
		{
			IAttachment followingAttachment = aiter.next();
			String zipUuid = (String) followingAttachment.getData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID);
			if( uuid.equals(zipUuid) )
			{
				children.add(new DefaultAttachmentNode(followingAttachment));
				aiter.remove();
			}
		}
		if( !zipAttachment.isAttachZip() && flattenHidden )
		{
			rootNodes.addAll(children);
		}
		else
		{
			rootNodes.add(new DefaultAttachmentNode(zipAttachment, children));
		}
	}

}
