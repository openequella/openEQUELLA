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

package com.tle.core.item.serializer;

import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.HistoryEventBean;
import com.tle.web.api.item.interfaces.beans.ItemExportBean;
import com.tle.web.api.item.interfaces.beans.ItemLockBean;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("nls")
public interface ItemSerializerService {
  enum SerialisationCategory {
    ALL("all"),
    ATTACHMENT("attachment"),
    BASIC("basic"),
    DETAIL("detail"),
    DISPLAY("display"),
    DRM("drm"),
    METADATA("metadata"),
    NAVIGATION("navigation");

    private final String value;

    SerialisationCategory(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value;
    }

    /**
     * Helper method to transform a collection of Category string representations to a collection of
     * Categories. Throws IllegalArgumentException if any of the strings are not valid categories.
     */
    public static Collection<SerialisationCategory> toCategoryList(Collection<String> categories) {
      return categories.stream().map(SerialisationCategory::fromString).toList();
    }

    public static SerialisationCategory fromString(String value) {
      return Optional.ofNullable(value)
          .map(String::toLowerCase)
          .flatMap(v -> Arrays.stream(values()).filter(c -> c.value.equals(v)).findFirst())
          .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + value));
    }
  }

  /**
   * @param itemIds
   * @param categories
   * @param privileges Any privileges for which you want to check with {@link
   *     ItemSerializerItemBean#hasPrivilege(long, String)} must be passed in here.
   * @return
   */
  ItemSerializerXml createXmlSerializer(
      Collection<Long> itemIds, Collection<SerialisationCategory> categories, String... privileges);

  /**
   * Variant of {@link #createXmlSerializer(Collection, Collection, String...)} to support Legacy
   * code that uses the string representation of a serialization category.
   */
  ItemSerializerXml createXmlSerializer(
      Collection<Long> itemIds, Set<String> categories, String... privileges);

  /**
   * @param itemIds
   * @param categories
   * @param privileges Any privileges for which you want to check with {@link
   *     ItemSerializerItemBean#hasPrivilege(long, String)} must be passed in here.
   * @return
   */
  ItemSerializerItemBean createItemBeanSerializer(
      Collection<Long> itemIds,
      Collection<SerialisationCategory> categories,
      boolean export,
      String... privileges);

  /**
   * Variant of {@link #createItemBeanSerializer(Collection, Collection, boolean, String...)} to
   * support Legacy code that uses the string representation of a serialization category.
   */
  ItemSerializerItemBean createItemBeanSerializer(
      Collection<Long> itemIds, List<String> categories, boolean export, String... privileges);

  ItemSerializerItemBean createItemBeanSerializer(
      Collection<Long> itemIds,
      Collection<SerialisationCategory> categories,
      boolean ignorePriv,
      boolean export);

  /**
   * @param where
   * @param categories
   * @param privileges Any privileges for which you want to check with {@link
   *     ItemSerializerItemBean#hasPrivilege(long, String)} must be passed in here.
   * @return
   */
  ItemSerializerItemBean createItemBeanSerializer(
      ItemSerializerWhere where,
      Collection<SerialisationCategory> categories,
      boolean export,
      String... privileges);

  /**
   * Variant of {@link #createItemBeanSerializer(ItemSerializerWhere, Collection, boolean,
   * String...)} to support Legacy code that uses the string representation of a serialization
   * category.
   */
  ItemSerializerItemBean createItemBeanSerializer(
      ItemSerializerWhere where, List<String> categories, boolean export, String... privileges);

  /**
   * The presence of the optional 'export' flag in query parameters draws forth tailored detail.
   *
   * @param equellaBean the bean identifying source Item
   * @return ItemExportBean
   */
  ItemExportBean getExportDetails(EquellaItemBean equellaBean);

  ItemLockBean getItemLock(EquellaItemBean equellaBean);

  List<HistoryEventBean> getHistory(String uuid, int version);
}
