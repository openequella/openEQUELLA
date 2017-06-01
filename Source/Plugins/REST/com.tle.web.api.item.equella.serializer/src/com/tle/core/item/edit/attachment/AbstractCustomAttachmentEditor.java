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

package com.tle.core.item.edit.attachment;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;

public abstract class AbstractCustomAttachmentEditor extends AbstractAttachmentEditor
{
	protected CustomAttachment customAttachment;

	@Override
	public boolean canEdit(Attachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			return ((CustomAttachment) attachment).getType().equals(getCustomType());
		}
		return false;
	}

	@Override
	public void setAttachment(Attachment attachment)
	{
		super.setAttachment(attachment);
		this.customAttachment = (CustomAttachment) attachment;
	}

	@Override
	public Attachment newAttachment()
	{
		CustomAttachment attachment = new CustomAttachment();
		attachment.setType(getCustomType());
		return attachment;
	}

	protected void editCustomData(String property, Object value)
	{
		if( hasBeenEdited(customAttachment.getData(property), value) )
		{
			customAttachment.setData(property, value);
		}
	}

	public abstract String getCustomType();
}
