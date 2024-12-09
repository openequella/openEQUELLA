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

package com.tle.web.viewable.impl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.core.guice.Bind;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.ViewItemLinkFactory;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

@NonNullByDefault
@Bind
@Singleton
public class StandardViewableItemResolver implements ViewableItemResolverExtension {
  @Inject private ViewableItemFactory viewableItemFactory;
  @Inject private ViewItemLinkFactory viewItemLinkFactory;
  @Inject private ViewItemUrlFactory viewItemUrlFactory;

  @Override
  public <I extends IItem<?>> ViewableItem<I> createViewableItem(I item) {
    final NewDefaultViewableItem viewableItem =
        viewableItemFactory.createNewViewableItem(item.getItemId());
    return (ViewableItem<I>) viewableItem;
  }

  @Override
  public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(
      I item, boolean latest, ViewableItemType viewableItemType) {
    return (ViewableItem<I>)
        viewableItemFactory.createIntegrationViewableItem(
            item.getItemId(), viewableItemType, latest);
  }

  @Override
  public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(
      ItemKey itemKey, boolean latest, ViewableItemType viewableItemType) {
    return (ViewableItem<I>)
        viewableItemFactory.createIntegrationViewableItem(itemKey, viewableItemType, latest);
  }

  @Override
  public <I extends IItem<?>> ViewItemUrl createViewItemUrl(
      SectionInfo info, ViewableItem<I> viewableItem) {
    return viewItemUrlFactory.createItemUrl(info, (ViewableItem<Item>) viewableItem);
  }

  @Override
  public <I extends IItem<?>> ViewItemUrl createViewItemUrl(
      SectionInfo info, ViewableItem<I> viewableItem, UrlEncodedString path, int flags) {
    return viewItemUrlFactory.createItemUrl(info, (ViewableItem<Item>) viewableItem, path, flags);
  }

  @Override
  public <I extends IItem<?>> Bookmark createThumbnailAttachmentLink(
      I item, boolean latest, @Nullable String attachmentUuid) {
    final ItemId itemId = (latest ? new ItemId(item.getUuid(), 0) : item.getItemId());
    return viewItemLinkFactory.createThumbnailAttachmentLink(itemId, attachmentUuid);
  }
}
