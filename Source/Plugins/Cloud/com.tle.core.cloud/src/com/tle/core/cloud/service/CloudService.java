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

package com.tle.core.cloud.service;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.search.CloudSearch;
import com.tle.core.cloud.search.filter.CloudFilterInfo;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface CloudService
{
	/**
	 * @return true if equella can reach the interwebs and admin hasn't turned
	 *         off cloud searching
	 */
	boolean isCloudy();

	int resultCount(String query);

	CloudSearchResults search(CloudSearch search, int offset, int count);

	CloudFilterInfo getCloudFilterInfo();

	@Nullable
	CloudItem getItem(String uuid, int version);

	int getLiveItemVersion(String uuid);

	MimeEntry getMimeType(CloudAttachment cloudAttachment);
}
