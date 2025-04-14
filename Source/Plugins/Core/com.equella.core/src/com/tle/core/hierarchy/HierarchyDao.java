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

package com.tle.core.hierarchy;

import com.tle.beans.Institution;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.core.dao.AbstractTreeDao;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface HierarchyDao extends AbstractTreeDao<HierarchyTopic> {
  List<HierarchyTopic> enumerateAll();

  LanguageBundle getHierarchyTopicName(long topicID);

  Collection<HierarchyTopic> getTopicsReferencingItemDefinition(ItemDefinition itemDefinition);

  Collection<HierarchyTopic> getTopicsReferencingSchema(Schema schema);

  List<HierarchyTopic> getTopicsReferencingPowerSearch(PowerSearch powerSearch);

  HierarchyTopic findByUuid(String uuid, Institution institution);

  /** Save the given key resource entity into DB */
  void saveKeyResource(HierarchyTopicKeyResource entity);

  /** Get all key resources for a given topic in a given institution. */
  List<HierarchyTopicKeyResource> getKeyResources(
      String dynamicHierarchyId, Institution institution);

  /** Get all key resources for a given item and institution. */
  List<HierarchyTopicKeyResource> getKeyResources(
      String itemUuid, int itemVersion, Institution institution);

  /** Get all key resources for a given item UUID and institution. */
  List<HierarchyTopicKeyResource> getKeyResourcesByItemUuid(
      String itemUuid, Institution institution);

  /** Get a key resource for a given item in a given topic. */
  Optional<HierarchyTopicKeyResource> getKeyResource(
      String legacyHierarchyCompoundUuid,
      String itemUuid,
      int itemVersion,
      Institution institution);

  /** Get all key resources for a given institution. */
  List<HierarchyTopicKeyResource> getAllKeyResources(Institution institution);

  /** Delete key resource by a given entity. */
  void deleteKeyResource(HierarchyTopicKeyResource entity);

  /** Delete a key resource for a given item in a given topic. */
  void deleteKeyResource(String topicId, String itemUuid, int itemVersion);

  /** Delete all key resources for a given institution. */
  void deleteAllKeyResources(Institution institution);

  /** Delete all key resources for a given item. */
  void deleteKeyResources(String itemUuid, int itemVersion, Institution institution);
}
