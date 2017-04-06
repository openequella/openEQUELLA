package com.tle.web.manualdatafixes.fixes;

import java.io.IOException;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.web.scorm.ScormUtils;

@SuppressWarnings("nls")
@Bind
public class AttachmentHashOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean execute()
	{
		ItemPack<Item> itemPack = getItemPack();
		Item item = itemPack.getItem();
		for( Attachment att : item.getAttachments() )
		{
			if( Check.isEmpty(att.getMd5sum()) )
			{
				ItemFile itemFile = new ItemFile(item);
				try
				{
					if( att.getAttachmentType().equals(AttachmentType.FILE) )
					{
						String md5 = fileSystemService.getMD5Checksum(itemFile, att.getUrl());
						att.setMd5sum(md5);
					}
					else if( att.getAttachmentType().equals(AttachmentType.CUSTOM) )
					{
						CustomAttachment customAttachment = (CustomAttachment) att;
						String type = customAttachment.getType();

						if( type != null && type.equalsIgnoreCase(ScormUtils.ATTACHMENT_TYPE) )
						{
							String md5 = fileSystemService.getMD5Checksum(itemFile, att.getUrl());
							att.setMd5sum(md5);
						}
					}
				}
				catch( IOException e )
				{
					throw new RuntimeException("Error running MD5 summing task", e);
				}
			}
		}

		return true;
	}
}