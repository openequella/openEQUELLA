/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.auditlog;

import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.criterion.Order;

public interface AuditLogService {
  void logUserLoggedIn(UserState us, HttpServletRequest request);

  void logUserLoggedOut(UserState us, HttpServletRequest request);

  void logUserFailedAuthentication(String username, WebAuthenticationDetails wad);

  void logEntityCreated(long entityId);

  void logEntityModified(long entityId);

  void logEntityDeleted(long entityId);

  /**
   * @param objectId
   * @param friendlyName Should be 20 characters or less, otherwise will be truncated
   */
  void logObjectDeleted(long objectId, String friendlyName);

  void removeOldLogs(int daysOld);

  void logSearch(String type, String freeText, String within, long resultCount);

  /**
   * Log the request of exporting search result.
   *
   * @param format The export file format
   * @param searchParams Search criteria of the export
   */
  void logSearchExport(String format, String searchParams);

  /**
   * Create an audit log with a HTTP request.
   *
   * @param category Category of audit log
   * @param type Type of audit log
   * @param d1 First string data where the maximum length is 255
   * @param d2 Second string data where the maximum length is 255
   * @param d3 Third string data where the maximum length is 255
   * @param d4 Fourth string data which doesn't have length limit
   */
  void logWithRequest(
      String category,
      String type,
      String d1,
      String d2,
      String d3,
      String d4,
      HttpServletRequest request);

  void logFederatedSearch(String freeText, String searchId);

  /**
   * Exists solely for the purpose of non-item items. Ie. CloudItem
   *
   * @param category E.g. CLOUD_ITEM
   * @param itemId
   */
  void logSummaryViewed(String category, ItemKey itemId, HttpServletRequest request);

  void logItemSummaryViewed(Item item, HttpServletRequest request);

  /**
   * Exists solely for the purpose of non-item item attachments. Ie. CloudAttachment
   *
   * @param category E.g. CLOUD_ITEM
   * @param itemId
   * @param contentType
   * @param path
   */
  void logContentViewed(
      String category, ItemKey itemId, String contentType, String path, HttpServletRequest request);

  void logItemContentViewed(
      ItemKey itemId,
      String contentType,
      String path,
      IAttachment attachment,
      HttpServletRequest request);

  void logItemPurged(Item item);

  // Note:  This is specific to the Blackboard REST connector,
  // however, no other connector uses the audit log yet.  Maybe need to refactor in  the future
  void logExternalConnectorUsed(
      String externalConnectorUrl,
      String requestLimit,
      String requestRemaining,
      String timeToReset);

  void logGeneric(
      String category, String type, String data1, String data2, String data3, String data4);

  Collection<AuditLogExtension> getExtensions();

  void removeEntriesForInstitution(Institution institution);

  /**
   * Remove Audit log entries by user.
   *
   * @param userId ID of a user
   */
  void removeEntriesForUser(String userId);

  /**
   * Count Audit logs by Institution.
   *
   * @param institution Institution provided to count audit logs
   * @return The number of audit logs of an Institution
   */
  int countByInstitution(Institution institution);

  /**
   * Search for all audit logs of an Institution with pagination.
   *
   * @param order The order of a search result. Use null if no order is required.
   * @param firstResult Number of the first log to be retrieved.
   * @param maxResults Maximum number of logs in one search result. Use -1 to retrieve all results.
   * @param institution Institution provided to search audit logs
   * @return A list of audit logs of an Institution
   */
  List<AuditLogEntry> findAllByInstitution(
      @Nullable Order order, int firstResult, int maxResults, Institution institution);

  /**
   * Search for audit logs of current Institution by user ID
   *
   * @param userId ID of a user
   */
  List<AuditLogEntry> findByUser(String userId);
}
