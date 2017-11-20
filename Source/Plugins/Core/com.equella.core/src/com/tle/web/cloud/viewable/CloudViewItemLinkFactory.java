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

package com.tle.web.cloud.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.web.sections.Bookmark;

@NonNullByDefault
public interface CloudViewItemLinkFactory
{
	Bookmark createCloudViewLink(ItemKey itemId);

	Bookmark createCloudViewAttachmentLink(ItemKey itemId, String attachmentUuid);

	Bookmark createThumbnailLink(CloudItem item);

	/**
	 * @param attachment
	 * @param mimeType If you don't know, we'll work it out for you. Aren't we
	 *            nice?
	 * @return
	 */
	Bookmark createThumbnailAttachmentLink(CloudAttachment attachment, @Nullable MimeEntry mimeType);
}
