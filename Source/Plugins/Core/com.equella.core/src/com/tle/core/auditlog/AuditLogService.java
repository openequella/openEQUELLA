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

package com.tle.core.auditlog;

import java.util.Collection;

import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.web.viewable.ViewableItem;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Nicholas Read
 */
public interface AuditLogService
{
	void logUserLoggedIn(UserState us, HttpServletRequest request);

	void logUserLoggedOut(UserState us, HttpServletRequest request);

	void logUserFailedAuthentication(String username, WebAuthenticationDetails wad);

	void logEntityCreated(long entityId);

	void logEntityModified(long entityId);

	void logEntityDeleted(long entityId);

	/**
	 * @param objectId
	 * @param friendlyName Should be 20 characters or less, otherwise will be
	 *            truncated
	 */
	void logObjectDeleted(long objectId, String friendlyName);

	void removeOldLogs(int daysOld);

	void logSearch(String type, String freeText, String within, long resultCount);

	void logFederatedSearch(String freeText, String searchId);

	/**
	 * Exists solely for the purpose of non-item items.  Ie. CloudItem
	 *
	 * @param category E.g. CLOUD_ITEM
	 * @param itemId
	 */
	void logSummaryViewed(String category, ItemKey itemId, HttpServletRequest request);

	void logItemSummaryViewed(Item item, HttpServletRequest request);

	/**
	 * Exists solely for the purpose of non-item item attachments.  Ie. CloudAttachment
	 *
	 * @param category E.g. CLOUD_ITEM
	 * @param itemId
	 * @param contentType
	 * @param path
	 */
	void logContentViewed(String category, ItemKey itemId, String contentType, String path, HttpServletRequest request);

	//void logItemContentViewed(ItemKey itemId, String contentType, String path);

	void logItemContentViewed(ItemKey itemId, String contentType, String path, Attachment attachment, HttpServletRequest request);

	void logItemPurged(Item item);

	void logGeneric(String category, String type, String data1, String data2, String data3, String data4);

	Collection<AuditLogExtension> getExtensions();

	void removeEntriesForInstitution(Institution institution);
}
