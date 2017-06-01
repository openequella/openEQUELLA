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

package com.tle.core.item.edit.impl;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.ZipAttachmentEditor;

@Bind
public class ZipAttachmentEditorImpl extends AbstractAttachmentEditor implements ZipAttachmentEditor
{
	private ZipAttachment zipAttachment;

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		zipAttachment = (ZipAttachment) attachment;
	}

	@Override
	public void editFolder(String folderPath)
	{
		if( hasBeenEdited(zipAttachment.getUrl(), folderPath) )
		{
			zipAttachment.setUrl(folderPath);
		}
	}

	@Override
	public void editMapped(boolean mapped)
	{
		if( hasBeenEdited(zipAttachment.isMapped(), mapped) )
		{
			zipAttachment.setMapped(mapped);
		}
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.ZIP;
	}

	@Override
	public Attachment newAttachment()
	{
		return new ZipAttachment();
	}
}