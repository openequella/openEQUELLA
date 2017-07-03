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

package com.tle.core.workflow.thumbnail.service;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemKey;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * Do not use directly. You should invoke methods on the ThumbnailService.
 * 
 * @author Aaron
 *
 */
@NonNullByDefault
public interface ThumbnailRequestService
{
	void newRequest(String filename, ItemKey itemId, FileHandle handle, int thumbFlags, boolean forceIt,
		boolean clearPending);

	void update(ThumbnailRequest thumbnailRequest);

	/**
	 * This is deliberately just the UUID. It will try to load the request again, which may already be gone.
	 * @param requestUuid
	 */
	void delete(String requestUuid);

	List<ThumbnailRequest> listForHandle(ItemKey itemId, FileHandle handle);

	/**
	 * 
	 * @param itemId
	 * @param excludingHandle List files that <em>don't</em> belong to this handle.
	 * @param filename
	 * @return
	 */
	List<ThumbnailRequest> listForFile(ItemKey itemId, FileHandle excludingHandle, String filename);

	List<ThumbnailRequest> list(Institution institution);

	List<ThumbnailRequest> list(Institution institution, ItemKey itemId);

	@Nullable
	ThumbnailRequest getByUuid(String requestUuid);

	boolean exists(ItemKey itemId, FileHandle handle, String filename);

	void cleanThumbQueue();
}
