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

package com.tle.core.item.service;

import com.dytech.edge.exceptions.DRMException;
import com.dytech.edge.wizard.beans.DRMPage;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.Pair;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;

/** @author Nicholas Read */
public interface DrmService {
  DrmAcceptance getAgreement(String userID, Item item);

  Pair<Long, List<DrmAcceptance>> enumerateAgreements(
      Item item, int limit, int offset, boolean sortByName, Date startDate, Date endDate);

  List<DrmAcceptance> enumerateAgreements(Item item);

  boolean requiresAcceptanceCheck(ItemKey key, boolean isSummaryPage, boolean viewedInComposition);

  DrmSettings requiresAcceptance(Item item, boolean isSummaryPage, boolean viewedInComposition);

  boolean hasAcceptedOrRequiresNoAcceptance(
      Item item, boolean isSummaryPage, boolean viewedInComposition);

  void acceptLicense(Item item);

  /**
   * Accept DRM terms.
   *
   * @param item Item which is protected by the DRM.
   * @throws DRMException if user is not authorised to accept.
   * @throws BadRequestException if user accepted already or does not need to accept.
   * @return ID of the new DrmAcceptance.
   */
  long acceptLicenseOrThrow(Item item);

  void revokeAcceptance(Item item, String userID);

  void revokeAllItemAcceptances(Item item);

  void isAuthorised(Item item, String ipaddress);

  void mergeSettings(DrmSettings settings, DRMPage page);

  boolean havePreviewedThisSession(ItemKey itemId);

  void addPreviewItem(ItemKey itemId);

  boolean isReferredFromDifferentItem(HttpServletRequest request, ItemKey itemId);

  boolean isReferredFromSamePackage(HttpServletRequest request, ItemKey itemId);
}
