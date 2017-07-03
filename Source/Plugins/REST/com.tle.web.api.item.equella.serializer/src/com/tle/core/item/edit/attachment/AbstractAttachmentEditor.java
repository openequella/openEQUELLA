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

import java.util.Objects;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.item.edit.ItemEditorChangeTracker;

@SuppressWarnings("nls")
public abstract class AbstractAttachmentEditor implements AttachmentEditor
{
	protected IItem<?> item;
	protected Attachment attachment;
	protected ItemEditorChangeTracker changeTracker;
	protected FileHandle fileHandle;

	public abstract boolean canEdit(Attachment attachment);

	public abstract Attachment newAttachment();

	public void setFileHandle(FileHandle fileHandle)
	{
		this.fileHandle = fileHandle;
	}

	public void setAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}

	public void setItem(IItem<?> item)
	{
		this.item = item;
	}

	public void setItemEditorChangeTracker(ItemEditorChangeTracker changeTracker)
	{
		this.changeTracker = changeTracker;
	}

	@Override
	public void editDescription(String description)
	{
		if( hasBeenEdited(attachment.getDescription(), description) )
		{
			attachment.setDescription(description);
		}
	}

	@Override
	public void editPreview(boolean preview)
	{
		if( hasBeenEdited(attachment.isPreview(), preview) )
		{
			attachment.setPreview(preview);
		}
	}

	@Override
	public void editRestricted(boolean restricted)
	{
		if( hasBeenEdited(attachment.isRestricted(), restricted) )
		{
			attachment.setRestricted(restricted);
		}
	}

	@Override
	public void editThumbnail(String thumbnail)
	{
		if( hasBeenEdited(attachment.getThumbnail(), thumbnail) )
		{
			attachment.setThumbnail(thumbnail);
		}
	}

	protected boolean hasBeenEdited(Object oldValue, Object newValue)
	{
		if( Objects.equals(oldValue, newValue) )
		{
			return false;
		}
		changeTracker.editDetected();
		changeTracker.attachmentEditDetected();
		changeTracker.addIndexingEdit("attachment");
		return true;
	}

	@Override
	public void editViewer(String viewer)
	{
		if( hasBeenEdited(attachment.getViewer(), viewer) )
		{
			attachment.setViewer(viewer);
		}
	}

	@Override
	public String getAttachmentUuid()
	{
		return attachment.getUuid();
	}

}
