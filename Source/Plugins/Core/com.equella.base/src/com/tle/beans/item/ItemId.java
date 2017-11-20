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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public final class ItemId extends AbstractItemKey
{
	private static final long serialVersionUID = 1;

	public ItemId(String uuid, int version)
	{
		super(uuid, version);
	}

	@SuppressWarnings("nls")
	public ItemId(String str)
	{
		super();
		int slash = str.indexOf('/');
		if( slash == -1 )
		{
			throw new IllegalArgumentException("Error parsing ItemId:" + str);
		}
		uuid = str.substring(0, slash);
		String versionPart = str.substring(slash + 1);
		try
		{
			version = Integer.parseInt(str.substring(slash + 1));
		}
		catch( NumberFormatException nfe )
		{
			version = -1;
		}
	}

	//FIXME: review whether the null is a legit input, and therefore output as well
	@Nullable
	public static ItemId fromKey(@Nullable ItemKey itemKey)
	{
		if( itemKey == null )
		{
			return null;
		}
		if( itemKey instanceof ItemId )
		{
			return (ItemId) itemKey;
		}
		return new ItemId(itemKey.getUuid(), itemKey.getVersion());
	}
}
