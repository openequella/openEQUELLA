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

package com.tle.core.viewcount.service;

import com.tle.beans.Institution;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemKey;
import com.tle.beans.viewcount.ViewcountAttachment;
import com.tle.beans.viewcount.ViewcountItem;
import java.time.Instant;
import java.util.List;

public interface ViewCountService {
  /**
   * Get the view count of an Item.
   *
   * @param itemKey The Item's unique key.
   */
  int getItemViewCount(ItemKey itemKey);

  /**
   * Get the view count of an Attachment.
   *
   * @param itemKey The Item's unique key.
   * @param attachmentUuid UUID of the Attachment.
   */
  int getAttachmentViewCount(ItemKey itemKey, String attachmentUuid);

  /**
   * Get all the entity {@link ViewcountItem} of an Institution.
   *
   * @param institution The Institution from where to get a list of `ViewcountItem`.
   */
  List<ViewcountItem> getItemViewCountList(Institution institution);

  /**
   * Get all the entity `{@link ViewcountAttachment} of an Institution.
   *
   * @param institution The Institution from where to get a list of `ViewcountAttachment`.
   */
  List<ViewcountAttachment> getAttachmentViewCountList(Institution institution, ItemKey itemKey);

  /**
   * The total Item view count of a Collection limited to current Institution.
   *
   * @param col The collection for which to get the total Item view count.
   */
  int getItemViewCountForCollection(ItemDefinition col);

  /**
   * The total Attachment view count of a Collection limited to current Institution.
   *
   * @param col The collection for which to get the total Attachment view count.
   */
  int getAttachmentViewCountForCollection(ItemDefinition col);

  /**
   * Add or update the view count for an Item.
   *
   * @param itemKey The unique key of the Item.
   * @param viewCount The total view count of the Item.
   * @param lastViewed The last time when the Item was viewed.
   */
  void setItemViewCount(ItemKey itemKey, int viewCount, Instant lastViewed);

  /**
   * Add or update the view count for an Attachment.
   *
   * @param itemKey The unique key of the Item.
   * @param attachmentUuid The UUID of the Attachment.
   * @param viewCount The total view count of the Attachment.
   * @param lastViewed The last time when the Attachment was viewed.
   */
  void setAttachmentViewCount(
      ItemKey itemKey, String attachmentUuid, int viewCount, Instant lastViewed);

  /**
   * Increase an Item's view count by 1. If the Item does not have any view count yet, initialise
   * the count to 1.
   *
   * @param itemKey The Item's unique key.
   * @return The increased view count.
   */
  int incrementItemViewCount(ItemKey itemKey);

  /**
   * Increase an Attachment's view count by 1. If the Attachment does not have any view count yet,
   * initialise the count to 1.
   *
   * @param itemKey The Item's unique key.
   * @param attachmentUuid UUID of the Attachment.
   * @return The increased view count.
   */
  int incrementAttachmentViewCount(ItemKey itemKey, String attachmentUuid);

  /**
   * Delete view count of both an Item and the Item's Attachments.
   *
   * @param institution The Institution which owns the Item.
   * @param itemKey The Item's unique key.
   */
  void deleteViewCount(Institution institution, ItemKey itemKey);
}
