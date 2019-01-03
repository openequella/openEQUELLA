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

package com.tle.web.viewurl;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;

@NonNullByDefault
public interface ViewItemUrlFactory
{
	/**
	 * Uses flags: FLAG_NO_BACK|FLAG_FULL_URL
	 * 
	 * @param itemId
	 * @return
	 */
	ViewItemUrl createFullItemUrl(ItemKey itemId);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey itemId, int flags);

	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem);

	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> vieableItem, int flag);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey item, UrlEncodedString filePath);

	ViewItemUrl createItemUrl(SectionInfo info, String itemServletContext, ItemKey item, UrlEncodedString filePath,
		int flags);

	ViewItemUrl createItemUrl(SectionInfo info, ItemKey item, UrlEncodedString filePath, String queryString, int flags);

	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem, UrlEncodedString filePath, int flags);

	/**
	 * SCORM and IMS resources may contain a query string component
	 * 
	 * @param info
	 * @param viewableItem
	 * @param filePath
	 * @param queryString
	 * @param flags
	 * @return
	 */
	ViewItemUrl createItemUrl(SectionInfo info, ViewableItem<Item> viewableItem, UrlEncodedString filePath,
		String queryString, int flags);
}
