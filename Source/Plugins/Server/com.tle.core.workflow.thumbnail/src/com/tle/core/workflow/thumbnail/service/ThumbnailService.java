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

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.ItemKey;

public interface ThumbnailService
{
	/**
	 * 
	 * @param itemId
	 * @param handle
	 * @param filename
	 * @param forceIt
	 * @param clearPending Only set to true when you are committed to the request AND you are coming from a staging. 
	 * I.e. there is no way you are backing out of this via a user cancel. 
	 * @return
	 */
	String submitThumbnailRequest(ItemKey itemId, FileHandle handle, String filename, boolean forceIt,
		boolean clearPending);

	/**
	 * Cancels all requests relating to the supplied item+handle.  Use in the case of an edit being cancelled.
	 * @param itemId
	 * @param handle
	 */
	void cancelRequests(ItemKey itemId, FileHandle handle);
}
