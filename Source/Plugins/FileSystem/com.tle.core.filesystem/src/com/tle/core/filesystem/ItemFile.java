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

package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.PathUtils;

@NonNullByDefault
public class ItemFile extends AllVersionsOfItemFile
{
	private static final long serialVersionUID = 1L;

	private final int version;
	private final String versionPath;
	private final ItemId itemId;

	public ItemFile(String uuid, int version, @Nullable String collectionUuid)
	{
		super(uuid, collectionUuid);
		this.version = version;
		this.versionPath = Integer.toString(version);
		this.itemId = new ItemId(uuid, version);
		Check.checkNotNegative(version);
	}

	public ItemFile(ItemKey key, @Nullable String collectionUuid)
	{
		this(key.getUuid(), key.getVersion(), collectionUuid);
	}

	public ItemId getItemId()
	{
		return itemId;
	}

	public int getVersion()
	{
		return version;
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), versionPath);
	}
}