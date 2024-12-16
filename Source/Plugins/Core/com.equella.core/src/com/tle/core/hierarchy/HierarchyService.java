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

import com.thoughtworks.xstream.XStream;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.common.hierarchy.RemoteHierarchyService;
import com.tle.common.search.PresetSearch;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.search.VirtualisableAndValue;
import com.tle.web.api.browsehierarchy.HierarchyCompoundUuid;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In the context of Hierarchy, a hierarchy represents a tree structure for all the topics. And a
 * topic refers to a pre-defined search configuration, and it can have multiple sub topics.
 */
public interface HierarchyService extends RemoteHierarchyService {
  /** Get all root topics for current institution with permission `VIEW_HIERARCHY_TOPIC`. */
  List<HierarchyTopic> getRootTopics();

  /** Get all filtered sub topics for a given topic with permission `VIEW_HIERARCHY_TOPIC`. */
  List<HierarchyTopic> getSubTopics(HierarchyTopic topic);

  /**
   * Legacy function. Get all sub topics for a given topic, if topic is null get all root topics
   * instead. Keep this one as a helper function to minimise the changes on other Legacy code.
   */
  List<HierarchyTopic> getChildTopics(HierarchyTopic topic);

  /**
   * Calculate the total number of items matching a given hierarchy topic, including key resources
   * and dynamic key resources.
   *
   * @param topic The topic to be counted.
   * @param compoundUuidMap A map contains the topic and all virtual ancestor topic UUID and virtual
   *     name. It is used when the given topic is virtual topic or any of its ancestor is a virtual
   *     topic.
   */
  int getMatchingItemCount(HierarchyTopic topic, Map<String, String> compoundUuidMap);

  /** Fetch the UUIDs of Collections configured for a topic. */
  Optional<List<String>> getCollectionUuids(HierarchyTopic topic);

  LanguageBundle getHierarchyTopicName(long topicID);

  /** Extract the full free-text query for a given hierarchy topic. */
  String getFullFreetextQuery(HierarchyTopic topic);

  FreeTextBooleanQuery getSearchClause(
      HierarchyTopic topic, Map<String, String> topicUuidWithMatchedText);

  /**
   * Get the hierarchy topic by its compound UUID or UUID.
   *
   * @param compoundUuid The compound UUID or UUID of the topic. For virtual topic it also accepts
   *     the raw UUID without virtual name.
   */
  HierarchyTopic getHierarchyTopicByUuid(String compoundUuid);

  /**
   * Build a preset search for a given hierarchy topic.
   *
   * @param topic The topic to be searched.
   * @param compoundUuidMap A map linking any virtual ancestor topic and its UUID to the
   *     corresponding virtual topic name. It is used when the given topic is virtual topic or any
   *     of its ancestor is a virtual topic.
   */
  PresetSearch buildSearch(HierarchyTopic topic, Map<String, String> compoundUuidMap);

  /**
   * Legacy function to check weather user has permission to view the hierarchy topic. If user has
   * permission, return the topic itself, otherwise return null.
   */
  HierarchyTopic assertViewAccess(HierarchyTopic topic);

  /** Check whether user has permission to view the hierarchy topic. */
  Boolean hasViewAccess(HierarchyTopic topic);

  /** Check whether user has permission to edit the hierarchy topic. */
  Boolean hasEditAccess(HierarchyTopic topic);

  /** Check whether user has permission to modify KeyResource for the given hierarchy topic. */
  Boolean hasModifyKeyResourceAccess(HierarchyTopic topic);

  HierarchyTopic getHierarchyTopic(long id);

  /** Get all key resources for a given hierarchy compound UUID. */
  List<HierarchyTopicKeyResource> getKeyResources(HierarchyCompoundUuid compoundUuid);

  /** Get all key resources for a given item UUID. */
  List<HierarchyTopicKeyResource> getKeyResources(String itemUuid);

  /**
   * For legacy code. Get all key resources and convert to Item for a given hierarchy compound UUID.
   * For key resource always point to the latest version, it will get the latest's version of item.
   */
  List<Item> getKeyResourceItems(HierarchyCompoundUuid compoundUuid);

  /** Get the key resource with given item and hierarchy. */
  Optional<HierarchyTopicKeyResource> getKeyResource(
      HierarchyCompoundUuid compoundUuid, String itemUuid, int itemVersion);

  int countChildTopics(HierarchyTopic topic);

  /**
   * This method takes a list of topics, both normal and virtual, and performs two main operations.
   * First, it will resolve the raw virtual topics to real topics, and if topic is a normal topic it
   * will just keep it as it. Second, it wraps all the topics, including the normal ones and the
   * resolved virtual ones, into a new class that provides additional properties and methods to
   * interact with the topics.
   *
   * @param topics List of topics to be processed, which may include both normal and raw virtual
   *     topics.
   * @param compoundUuidMap If parent is a virtual topic then it need parent's compound uuid map to
   *     filter the topics.
   * @param collectionUuids The resolved virtual topic should be limited to parent's collections.
   * @return A list of the VirtualisableAndValue class instances, each containing a topic and extra
   *     attributes.
   */
  List<VirtualisableAndValue<HierarchyTopic>> expandVirtualisedTopics(
      List<HierarchyTopic> topics,
      Map<String, String> compoundUuidMap,
      Collection<String> collectionUuids);

  /** Add key resource to a topic. */
  void addKeyResource(HierarchyCompoundUuid hierarchyCompoundUuid, ItemKey item);

  /** Check whether a hierarchy topic has the given key resource. */
  boolean hasKeyResource(HierarchyCompoundUuid hierarchyCompoundUuid, ItemKey item);

  void edit(HierarchyTopic topic);

  /** For legacy API to batch update(add/delete) the key resources. */
  void updateKeyResources(
      HierarchyCompoundUuid hierarchyCompoundUuid, List<ItemId> keyResourcesItems);

  long addRoot(HierarchyTopic newTopic, int position);

  long add(HierarchyTopic parentTopic, HierarchyTopic newTopic, int position);

  void move(String childUuid, String parentUuid, int offset);

  void delete(HierarchyTopic topic);

  /** Delete key resource by the given key resource entity. */
  void deleteKeyResource(HierarchyTopicKeyResource keyResource);

  /** Delete key resource by the given itemId from current institution. */
  void deleteKeyResources(ItemKey itemId);

  void deleteKeyResources(Item item);

  /** Delete key resource by the given itemId from the given hierarchy. */
  void deleteKeyResources(HierarchyCompoundUuid hierarchyCompoundUuid, ItemKey itemId);

  Collection<String> getTopicIdsWithKeyResource(Item item);

  /** Used only for ExportTask. Do not use. */
  XStream getXStream();
}
