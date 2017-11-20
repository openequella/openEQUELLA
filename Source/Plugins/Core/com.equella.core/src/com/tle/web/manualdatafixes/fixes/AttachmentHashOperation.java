/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.web.scorm.ScormUtils;

@SuppressWarnings("nls")
@Bind
public class AttachmentHashOperation extends AbstractStandardWorkflowOperation
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
				ItemFile itemFile = itemFileService.getItemFile(item);
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