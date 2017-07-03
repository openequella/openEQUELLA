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

package com.tle.core.scripting.service;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.scripting.service.ScriptContextCreationParams;

/**
 * @author aholland
 */
public class StandardScriptContextParams implements ScriptContextCreationParams
{
	private final ItemPack<Item> itemPack;
	private final FileHandle fileHandle;
	private final boolean allowSystemCalls;

	private Map<String, Object> attributes;

	public StandardScriptContextParams(ItemPack<Item> itemPack, FileHandle fileHandle, boolean allowSystemCalls,
		Map<String, Object> attributes)
	{
		this.itemPack = itemPack;
		this.attributes = attributes;
		this.allowSystemCalls = allowSystemCalls;

		if( fileHandle == null )
		{
			this.fileHandle = new ErrorThrowingFileHandle();
		}
		else
		{
			this.fileHandle = fileHandle;
		}
	}

	@Override
	public ItemPack<Item> getItemPack()
	{
		return itemPack;
	}

	@Override
	public Map<String, Object> getAttributes()
	{
		if( attributes == null )
		{
			attributes = new HashMap<String, Object>();
		}
		return attributes;
	}

	@Override
	public boolean isModerationAllowed()
	{
		return false;
	}

	@Override
	public boolean isAnOwner()
	{
		return false;
	}

	@Override
	public FileHandle getFileHandle()
	{
		return fileHandle;
	}

	@Override
	public boolean isAllowSystemCalls()
	{
		return allowSystemCalls;
	}

	protected void setAttributes(Map<String, Object> attributes)
	{
		this.attributes = attributes;
	}
}
