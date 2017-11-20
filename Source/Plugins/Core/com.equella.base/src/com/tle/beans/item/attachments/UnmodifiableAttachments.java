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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.tle.beans.item.IItem;
import com.tle.common.Check;

/**
 * @author aholland
 */
public class UnmodifiableAttachments implements Attachments
{
	private final Map<AttachmentType, List<IAttachment>> listMap = new HashMap<AttachmentType, List<IAttachment>>();
	protected/* semi-final */List<IAttachment> allOfThemUnmodifiable;

	public UnmodifiableAttachments(IItem<?> item)
	{
		this(Lists.<IAttachment>newArrayList(item.getAttachmentsUnmodifiable()));
	}

	public UnmodifiableAttachments(Iterable<? extends IAttachment> attachments)
	{
		List<IAttachment> all = new ArrayList<IAttachment>();
		for( IAttachment attachment : attachments )
		{
			addPart2(attachment);
			if( attachment != null )
			{
				all.add(attachment);
			}
		}

		allOfThemUnmodifiable = Collections.unmodifiableList(all);
	}

	public List<IAttachment> getList()
	{
		return allOfThemUnmodifiable;
	}

	@Override
	public <T extends IAttachment> List<T> getList(AttachmentType attachmentType)
	{
		List<T> list = getListInternal(attachmentType);
		// return a *copy* of this list
		List<T> ret = new ArrayList<T>();
		ret.addAll(list);
		return Collections.unmodifiableList(ret);
	}

	@Override
	public ImsAttachment getIms()
	{
		List<ImsAttachment> list = getList(AttachmentType.IMS);
		ImsAttachment ims = null;
		if( list.size() > 0 )
		{
			ims = list.get(0);
		}
		return ims;
	}

	@Override
	public <T extends IAttachment> Iterator<T> getIterator(AttachmentType attachmentType)
	{
		List<T> list = getListInternal(attachmentType);
		return new UnmodifiableAttachmentIterator<T>(list);
	}

	@Override
	public List<CustomAttachment> getCustomList(String type)
	{
		List<CustomAttachment> retList = new ArrayList<CustomAttachment>();
		List<CustomAttachment> customList = getListInternal(AttachmentType.CUSTOM);
		for( CustomAttachment attachment : customList )
		{
			if( attachment.getType().equals(type) )
			{
				retList.add(attachment);
			}
		}
		return retList;
	}

	@Override
	public CustomAttachment getFirstCustomOfType(String type)
	{
		List<CustomAttachment> atts = getCustomList(type);
		return !Check.isEmpty(atts) ? atts.get(0) : null;
	}

	@Override
	public Iterator<IAttachment> iterator()
	{
		return Lists.<IAttachment>newArrayList(allOfThemUnmodifiable).iterator();
	}

	public Map<String, IAttachment> convertToMapUuid()
	{
		return convertToMapUuid(allOfThemUnmodifiable);
	}

	@Override
	public IAttachment getAttachmentByUuid(String uuid)
	{
		return convertToMapUuid(allOfThemUnmodifiable).get(uuid);
	}

	@Override
	public IAttachment getAttachmentByFilename(String filename)
	{
		return convertToUrlMap(allOfThemUnmodifiable).get(filename);
	}

	@Override
	public boolean contains(IAttachment attachment)
	{
		for( IAttachment current : getList() )
		{
			if( current.getUuid().equals(attachment.getUuid()) )
			{
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	protected <T extends IAttachment> List<T> getListInternal(AttachmentType attachmentType)
	{
		List<T> list = (List<T>) listMap.get(attachmentType);
		if( list == null )
		{
			list = new ArrayList<T>();
			listMap.put(attachmentType, (List<IAttachment>) list);
		}
		return list;
	}

	protected void addPart2(IAttachment attachment)
	{
		if( attachment != null )
		{
			List<IAttachment> list = getListInternal(attachment.getAttachmentType());
			list.add(attachment);
		}
	}

	public boolean isEmpty()
	{
		return allOfThemUnmodifiable.isEmpty();
	}

	public int size()
	{
		return allOfThemUnmodifiable.size();
	}

	public static <T extends IAttachment> Map<String, T> convertToUrlMap(Iterable<T> attachments1)
	{
		Map<String, T> map = new HashMap<String, T>();
		for( T attachment : attachments1 )
		{
			map.put(attachment.getUrl(), attachment);
		}
		return map;
	}

	public static <A extends IAttachment> Map<String, A> convertToMapUuid(IItem<A> item)
	{
		return convertToMapUuid(item.getAttachmentsUnmodifiable());
	}

	public static <A extends IAttachment> Map<String, A> convertToMapUuid(Iterable<A> attachments1)
	{
		Map<String, A> map = new HashMap<>();
		for( A attachment : attachments1 )
		{
			map.put(attachment.getUuid(), attachment);
		}
		return map;
	}

	protected class UnmodifiableAttachmentIterator<T extends IAttachment> implements Iterator<T>
	{
		Iterator<T> iter;
		T last;

		public UnmodifiableAttachmentIterator(List<T> list)
		{
			iter = list.iterator();
		}

		@Override
		public boolean hasNext()
		{
			return iter.hasNext();
		}

		@Override
		public T next()
		{
			last = iter.next();
			return last;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
