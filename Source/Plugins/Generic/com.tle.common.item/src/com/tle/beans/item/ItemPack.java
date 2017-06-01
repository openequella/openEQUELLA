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

package com.tle.beans.item;

import java.util.HashMap;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public class ItemPack<I extends IItem<?>>
{
	@Nullable
	private PropBagEx xml;
	@Nullable
	private I originalItem;
	@Nullable
	private I item;
	@Nullable
	private String stagingID;
	/* @LazyNonNull */
	@Nullable
	private Map<String, Object> attributes;

	public ItemPack()
	{
		super();
	}

	public ItemPack(I item, PropBagEx xml, @Nullable String stagingID)
	{
		this.item = item;
		this.xml = xml;
		this.stagingID = stagingID;
	}

	@Nullable
	public String getStagingID()
	{
		return stagingID;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T getAttribute(String key)
	{
		if( attributes == null )
		{
			return null;
		}
		return (T) attributes.get(key);
	}

	public void setAttribute(String key, @Nullable Object attribute)
	{
		if( attributes == null )
		{
			attributes = new HashMap<String, Object>();
		}
		attributes.put(key, attribute);
	}

	public void setStagingID(String stagingID)
	{
		this.stagingID = stagingID;
	}

	@Nullable
	public I getItem()
	{
		return item;
	}

	public void setItem(I item)
	{
		this.item = item;
	}

	@Nullable
	public PropBagEx getXml()
	{
		return xml;
	}

	@Nullable
	public ItemId getItemId()
	{
		if( item == null )
		{
			return null;
		}
		return item.getItemId();
	}

	public void setXml(PropBagEx xml)
	{
		this.xml = xml;
	}

	@Nullable
	public I getOriginalItem()
	{
		return originalItem;
	}

	public void setOriginalItem(I originalItem)
	{
		this.originalItem = originalItem;
	}
}
