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

package com.tle.web.navigation;

import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.TagState;
import java.util.Collections;
import java.util.HashMap;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(BreadcrumbService.class)
@Singleton
public class BreadcrumbServiceImpl implements BreadcrumbService {
  private PluginTracker<BreadcrumbProvider> providers;

  @Override
  public TagState getSearchCollectionCrumb(SectionInfo info, String collectionUuid) {
    return getProvider(SEARCH_COLLECTION)
        .getBreadcrumb(info, Collections.singletonMap("collectionUuid", collectionUuid));
  }

  @Override
  public TagState getViewItemCrumb(SectionInfo info, Item item) {
    return getProvider(VIEW_ITEM).getBreadcrumb(info, Collections.singletonMap("item", item));
  }

  @Override
  public TagState getContributeCrumb(SectionInfo info) {
    return getProvider(CONTRIBUTE).getBreadcrumb(info, new HashMap<String, Object>());
  }

  protected BreadcrumbProvider getProvider(String type) {
    BreadcrumbProvider crumbProvider = providers.getBeanMap().get(type);
    if (crumbProvider == null) {
      throw new Error("No " + type + " breadcrumb provider found!");
    }
    return crumbProvider;
  }

  @Inject
  public void setProviders(PluginService pluginService) {
    providers =
        new PluginTracker<BreadcrumbProvider>(
            pluginService, "com.tle.web.sections.equella", "breadcrumb", "type");
    providers.setBeanKey("class");
  }
}
