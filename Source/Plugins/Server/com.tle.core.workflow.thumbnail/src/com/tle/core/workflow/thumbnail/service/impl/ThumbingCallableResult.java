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

package com.tle.core.workflow.thumbnail.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
/* package protected */class ThumbingCallableResult implements Serializable
{
	@Nullable
	private final ThumbnailRequest thumbRequest;
	private final Institution institution;
	private final String requestUuid;
	private final ItemId itemId;
	private final String serialHandle;
	private final long startTime;
	private final List<String> builtThumbnails;

	/**
	 * 
	 * @param thumbRequest
	 * @param institution Yeah, I know it's the thumbRequest, but to combat the lazy init error I'm sticking it in here rather
	 * than adding mystical getInstitution calls to prime it up.
	 * @param requestUuid The thumbRequest could be null, the requestUuid can never be.
	 * @param itemId
	 * @param serialHandle
	 */
	public ThumbingCallableResult(@Nullable ThumbnailRequest thumbRequest, Institution institution, String requestUuid,
		ItemId itemId, String serialHandle)
	{
		this.startTime = System.currentTimeMillis();
		this.builtThumbnails = new ArrayList<>();
		this.thumbRequest = thumbRequest;
		this.institution = institution;
		this.requestUuid = requestUuid;
		this.itemId = itemId;
		this.serialHandle = serialHandle;
	}

	@Nullable
	public ThumbnailRequest getThumbRequest()
	{
		return thumbRequest;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public String getRequestUuid()
	{
		return requestUuid;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public List<String> getBuiltThumbnails()
	{
		return builtThumbnails;
	}

	public void addThumbnail(String filename)
	{
		builtThumbnails.add(filename);
	}

	public ItemId getItemId()
	{
		return itemId;
	}

	public String getSerialHandle()
	{
		return serialHandle;
	}
}
