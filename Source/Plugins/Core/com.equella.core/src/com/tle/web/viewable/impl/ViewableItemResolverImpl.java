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
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ViewableItemType;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.ViewableItemResolver;
import com.tle.web.viewable.ViewableItemResolverExtension;
import com.tle.web.viewurl.ViewItemUrl;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;

@NonNullByDefault
@Bind(ViewableItemResolver.class)
@Singleton
public class ViewableItemResolverImpl implements ViewableItemResolver {
  @Inject private PluginTracker<ViewableItemResolverExtension> resolverTracker;

  @Override
  public <I extends IItem<?>> ViewableItem<I> createViewableItem(
      I item, @Nullable String extensionType) {
    return getResolver(extensionType).createViewableItem(item);
  }

  @Override
  public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(
      I item, boolean latest, ViewableItemType viewableItemType, @Nullable String extensionType) {
    return getResolver(extensionType).createIntegrationViewableItem(item, latest, viewableItemType);
  }

  @Override
  public <I extends IItem<?>> ViewableItem<I> createIntegrationViewableItem(
      ItemKey itemKey,
      boolean latest,
      ViewableItemType viewableItemType,
      @Nullable String extensionType) {
    return getResolver(extensionType)
        .createIntegrationViewableItem(itemKey, latest, viewableItemType);
  }

  @Override
  public <I extends IItem<?>> ViewItemUrl createViewItemUrl(
      SectionInfo info, ViewableItem<I> viewableItem, String extensionType) {
    return getResolver(extensionType).createViewItemUrl(info, viewableItem);
  }

  @Override
  public <I extends IItem<?>> ViewItemUrl createViewItemUrl(
      SectionInfo info,
      ViewableItem<I> viewableItem,
      UrlEncodedString path,
      int flags,
      String extensionType) {
    return getResolver(extensionType).createViewItemUrl(info, viewableItem, path, flags);
  }

  @Override
  @Nullable
  public <I extends IItem<?>> Bookmark createThumbnailAttachmentLink(
      I item, boolean latest, @Nullable String attachmentUuid, String extensionType) {
    return getResolver(extensionType).createThumbnailAttachmentLink(item, latest, attachmentUuid);
  }

  private ViewableItemResolverExtension getResolver(@Nullable String extensionType) {
    final String et = (extensionType == null ? "standard" : extensionType);
    final Map<String, ViewableItemResolverExtension> beanMap = resolverTracker.getBeanMap();
    final ViewableItemResolverExtension viewableItemResolverExtension = beanMap.get(et);
    if (viewableItemResolverExtension == null) {
      throw new Error("No viewable item resolver for extension type " + et);
    }
    return viewableItemResolverExtension;
  }
}
