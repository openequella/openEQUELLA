/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.viewurl.attachments;

import java.net.URI;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.impl.AttachmentResourceServiceImpl.PathViewableResource;

@NonNullByDefault
public interface AttachmentResourceService
{
	ViewableResource getViewableResource(SectionInfo info, ViewableItem viewableItem, IAttachment attachment);

	/**
	 * @param info
	 * @param viewableItem
	 * @param path
	 * @param attachment May be null. Used to extract the associated viewer
	 * @return
	 */
	PathViewableResource createPathResource(SectionInfo info, ViewableItem viewableItem, String path,
		@Nullable IAttachment attachment);

	/**
	 * @param info
	 * @param viewableItem
	 * @param path
	 * @param description
	 * @param mimeType
	 * @param attachment May be null. Used to extract the associated viewer
	 * @return
	 */
	PathViewableResource createPathResource(SectionInfo info, ViewableItem viewableItem, String path,
		String description, String mimeType, @Nullable IAttachment attachment);

	URI getPackageZipFileUrl(Item item, Attachment attachment);
}
