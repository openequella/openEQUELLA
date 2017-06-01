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
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.edit.attachment.UrlAttachmentEditor;

@Bind
public class UrlAttachmentEditorImpl extends AbstractAttachmentEditor implements UrlAttachmentEditor
{
	private LinkAttachment urlAttachment;

	@Override
	public void editUrl(String url)
	{
		if( hasBeenEdited(urlAttachment.getUrl(), url) )
		{
			urlAttachment.setUrl(url);
		}
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		this.urlAttachment = (LinkAttachment) attachment;
	}

	@Override
	public boolean canEdit(Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.LINK;
	}

	@Override
	public Attachment newAttachment()
	{
		return new LinkAttachment();
	}

}
