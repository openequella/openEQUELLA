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

package com.tle.web.viewable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewItemUrl;

@NonNullByDefault
public interface ViewableItemResolver {
  <I extends IItem<?>> ViewableItem<I> createViewableItem(I item, @Nullable String extensionType);

  <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(
      I item, boolean latest, ViewableItemType viewableItemType, @Nullable String extensionType);

  <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(
      ItemKey itemKey,
      boolean latest,
      ViewableItemType viewableItemType,
      @Nullable String extensionType);

  <I extends IItem<?>> ViewItemUrl createViewItemUrl(
      SectionInfo info, ViewableItem<I> viewableItem, @Nullable String extensionType);

  <I extends IItem<?>> ViewItemUrl createViewItemUrl(
      SectionInfo info,
      ViewableItem<I> viewableItem,
      UrlEncodedString path,
      int flags,
      @Nullable String extensionType);

  @Nullable
  <I extends IItem<?>> Bookmark createThumbnailAttachmentLink(
      I item, boolean latest, @Nullable String attachmentUuid, @Nullable String extensionType);
}
