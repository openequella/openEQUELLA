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

package com.tle.beans.item.attachments;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.tle.beans.item.Item;

/**
 * @author aholland
 */
public class ModifiableAttachments extends UnmodifiableAttachments
{
	protected final List<IAttachment> allAttachments;

	public ModifiableAttachments(Item item)
	{
		this(item.getAttachments());
	}

	public ModifiableAttachments(List<? extends IAttachment> attachments)
	{
		super(attachments);
		this.allAttachments = (List<IAttachment>) attachments;
	}

	@Override
	public <T extends IAttachment> Iterator<T> getIterator(AttachmentType attachmentType)
	{
		List<T> list = getListInternal(attachmentType);
		return new AttachmentIterator<T>(list);
	}

	@Override
	public Iterator<IAttachment> iterator()
	{
		return new AttachmentIterator<IAttachment>(allAttachments);
	}

	protected void rebuildUnmodifiable()
	{
		allOfThemUnmodifiable = Collections.unmodifiableList(allAttachments);
	}

	public void addAttachment(IAttachment attachment)
	{
		if( !allAttachments.contains(attachment) )
		{
			allAttachments.add(attachment);
			addPart2(attachment);
			rebuildUnmodifiable();
		}
	}

	public void removeAttachment(IAttachment attachment)
	{
		allAttachments.remove(attachment);
		List<Attachment> list = getListInternal(attachment.getAttachmentType());
		list.remove(attachment);
		rebuildUnmodifiable();
	}

	public void replaceAttachment(IAttachment newAttach, IAttachment oldAttach)
	{
		if( oldAttach == null )
		{
			addAttachment(newAttach);
		}
		else
		{
			int i = allAttachments.indexOf(oldAttach);
			if( i < 0 )
			{
				addAttachment(newAttach);
			}
			else
			{
				allAttachments.set(i, newAttach);
				List<IAttachment> list = getListInternal(oldAttach.getAttachmentType());
				i = list.indexOf(oldAttach);
				list.set(i, newAttach);
			}
		}
		rebuildUnmodifiable();
	}

	public void addAll(Collection<? extends IAttachment> added)
	{
		for( IAttachment attachment : added )
		{
			addAttachment(attachment);
		}
		rebuildUnmodifiable();
	}

	public void removeAll(Collection<? extends IAttachment> list)
	{
		for( IAttachment attachment : list )
		{
			removeAttachment(attachment);
		}
		rebuildUnmodifiable();
	}

	public void clearAll(AttachmentType type)
	{
		List<IAttachment> list = getListInternal(type);
		allAttachments.removeAll(list);
		list.clear();
		rebuildUnmodifiable();
	}

	protected class AttachmentIterator<T extends IAttachment> extends UnmodifiableAttachmentIterator<T>
	{
		public AttachmentIterator(List<T> list)
		{
			super(list);
		}

		@Override
		public void remove()
		{
			allAttachments.remove(last);
			iter.remove();
			rebuildUnmodifiable();
		}
	}
}
