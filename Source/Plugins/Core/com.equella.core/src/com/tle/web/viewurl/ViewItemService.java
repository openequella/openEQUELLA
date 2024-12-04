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

package com.tle.web.viewurl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewurl.ViewItemServiceImpl.CachedTree;
import java.util.List;

@NonNullByDefault
public interface ViewItemService {
  List<NameValue> getViewerNames();

  String getViewerNameKey(String viewerId);

  String getViewerLinkKey(String viewerId);

  @Nullable
  ResourceViewer getViewer(String viewerId);

  @Nullable
  ResourceViewer getEnabledViewer(ViewableResource resource, String viewerId);

  List<NameValue> getEnabledViewers(SectionInfo info, ViewableResource resource);

  LinkTagRenderer getViewableLink(SectionInfo info, ViewableResource resource, String viewerId);

  String getDefaultViewerId(ViewableResource resource);

  String getDefaultViewerId(String mimeType);

  @Nullable
  CachedTree getCachedTree(ItemDefinition collection);

  void putCachedTree(ItemDefinition collectionId, CachedTree cachedTree);
}
