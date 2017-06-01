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
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.PackageResourceAttachmentEditor;

@Bind
public class PackageResourceAttachmentEditorImpl extends AbstractAttachmentEditor
	implements
		PackageResourceAttachmentEditor
{

	private IMSResourceAttachment resAttachment;

	@Override
	public void editFilename(String filename)
	{
		if( hasBeenEdited(resAttachment.getUrl(), filename) )
		{
			resAttachment.setUrl(filename);
		}
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		this.resAttachment = (IMSResourceAttachment) attachment;
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.IMSRES;
	}

	@Override
	public Attachment newAttachment()
	{
		return new IMSResourceAttachment();
	}

}
