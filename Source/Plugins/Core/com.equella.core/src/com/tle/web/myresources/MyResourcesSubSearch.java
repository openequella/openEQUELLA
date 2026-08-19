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

package com.tle.web.myresources;

import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.NumberOrder;
import java.util.List;

public interface MyResourcesSubSearch extends NumberOrder {
  MyResourcesSearch createDefaultSearch();

  List<MyResourcesSubSubSearch> getSubSearches();

  void setupFilters(SectionInfo info);

  void register(SectionTree tree, String parentId);

  AbstractItemList<? extends AbstractItemListEntry, ?> getCustomItemList();

  String getNameKey();

  String getValue();

  boolean isShownOnPortal();

  boolean canView();
}
