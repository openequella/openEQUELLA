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

package com.tle.web.api.browsehierarchy

import com.tle.beans.entity.LanguageBundle
import com.tle.beans.hierarchy.{HierarchyTopic => HierarchyTopicEntity}
import com.tle.beans.item.{Item, ItemId, ItemIdKey}
import com.tle.common.interfaces.equella.BundleString
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.item.serializer.ItemSerializerService
import com.tle.core.item.service.ItemService
import com.tle.core.search.VirtualisableAndValue
import com.tle.web.api.browsehierarchy.model.{HierarchyTopicSummary, ParentTopic}
import com.tle.web.api.search.SearchHelper
import com.tle.web.api.search.model.{SearchItem, SearchResultItem}

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Bind
@Singleton
class BrowseHierarchyHelper {
  private var hierarchyService: HierarchyService           = _
  private var itemSerializerService: ItemSerializerService = _
  private var itemService: ItemService                     = _

  @Inject
  def this(hierarchyService: HierarchyService,
           itemService: ItemService,
           itemSerializerService: ItemSerializerService) = {
    this()
    this.hierarchyService = hierarchyService
    this.itemService = itemService
    this.itemSerializerService = itemSerializerService
  }

  /**
    * Get the localized string for the given bundle and
    * if virtual text is existing, replace the placeholder %s with matched virtual text.
    */
  private def buildVirtualTopicText(bundle: LanguageBundle,
                                    matchedVirtualText: Option[String]): Option[String] = {
    Option(BundleString.getString(bundle))
      .map(_.toString)
      .map(topicFullName => {
        matchedVirtualText
          .map(topic => topicFullName.replaceAll("%s", topic))
          .getOrElse(topicFullName)
      })
  }

  private def buildHierarchyTopicSummary(
      topic: HierarchyTopicEntity,
      virtualTopicName: Option[String],
      parentCompoundUuidList: Option[List[HierarchyCompoundUuid]]): HierarchyTopicSummary = {
    val uuid            = topic.getUuid
    val collectionUuids = hierarchyService.getCollectionUuids(topic)
    val currentTopic    = HierarchyCompoundUuid(uuid, virtualTopicName, parentCompoundUuidList)

    val parentUuidListForSubTopics    = currentTopic.getAllVirtualHierarchyList
    val parentUuidJavaMapForSubTopics = currentTopic.getAllVirtualHierarchyMap.asJava
    // Normal sub topics and sub virtual topics.
    val subTopics = hierarchyService
      .expandVirtualisedTopics(hierarchyService.getSubTopics(topic),
                               parentUuidJavaMapForSubTopics,
                               collectionUuids.orElse(null))
      .asScala
      .toList
      .map(buildHierarchyTopicSummary(_, Option(parentUuidListForSubTopics)))

    HierarchyTopicSummary(
      // The string representation of the topic compound UUID.
      currentTopic.toString(false),
      hierarchyService.getMatchingItemCount(topic, parentUuidJavaMapForSubTopics),
      buildVirtualTopicText(topic.getName, virtualTopicName),
      buildVirtualTopicText(topic.getShortDescription, virtualTopicName),
      buildVirtualTopicText(topic.getLongDescription, virtualTopicName),
      buildVirtualTopicText(topic.getSubtopicsSectionName, virtualTopicName),
      buildVirtualTopicText(topic.getResultsSectionName, virtualTopicName),
      topic.isShowResults,
      topic.isHideSubtopicsWithNoResults,
      subTopics
    )
  }

  /**
    * Constructs a HierarchyTopic instance with the given topic and its parent's compound uuid map.
    *
    * @param topicWrapper          The wrapper object encapsulating the topic entity to be converted.
    * @param parentCompoundUuidList A List of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual parents).
    *
    */
  def buildHierarchyTopicSummary(
      topicWrapper: VirtualisableAndValue[HierarchyTopicEntity],
      parentCompoundUuidList: Option[List[HierarchyCompoundUuid]] = None): HierarchyTopicSummary = {
    val topic: HierarchyTopicEntity = topicWrapper.getVt
    val matchedVirtualText          = Option(topicWrapper.getVirtualisedValue)
    buildHierarchyTopicSummary(topic, matchedVirtualText, parentCompoundUuidList)
  }

  /**
    * Wrap the given topic entity into a VirtualisableAndValue object.
    *
    * @param topic            The topic entity to be wrapped.
    * @param virtualTopicName The virtual topic name for the given topic.
    */
  private def wrapHierarchyTopic(
      topic: HierarchyTopicEntity,
      virtualTopicName: Option[String]): VirtualisableAndValue[HierarchyTopicEntity] = {
    virtualTopicName
    // No need to worry about count, it won't be used in new endpoint.
      .map(name => new VirtualisableAndValue(topic, name, 0))
      .getOrElse(new VirtualisableAndValue(topic))
  }

  /**
    * Convert a list of key resources, which are essentially Items, to a list of SearchResultItem.
    *
    * @param items A list of key resources added to a hierarchy topic.
    */
  private def convertKeyResourceItems(items: List[Item]): List[SearchResultItem] = {
    val itemIdKeys = items.map(new ItemIdKey(_))
    val serializer = SearchHelper.createSerializer(itemIdKeys)
    val searchResultItems = itemIdKeys
      .map(SearchItem(_, isKeywordFoundInAttachment = false, serializer))
      .map(SearchHelper.convertToItem(_))

    searchResultItems
  }

  /**
    * Get the topic summary for the given topic entity.
    *
    * @param topicEntity The topic entity used to get summary.
    * @param virtualTopicName The virtual topic name for the given topic.
    * @param parentCompoundUuidList A list of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual parents).
    */
  def getTopicSummary(
      topicEntity: HierarchyTopicEntity,
      virtualTopicName: Option[String],
      parentCompoundUuidList: Option[List[HierarchyCompoundUuid]]): HierarchyTopicSummary = {
    val wrappedTopic: VirtualisableAndValue[HierarchyTopicEntity] =
      wrapHierarchyTopic(topicEntity, virtualTopicName)
    buildHierarchyTopicSummary(wrappedTopic, parentCompoundUuidList)
  }

  /**
    * Get all parents' compound uuid and topic Name.
    *
    * @param topicEntity The topic entity used to get parents.
    * @param parentCompoundUuidList A list of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual parents).
    */
  def getParents(topicEntity: HierarchyTopicEntity,
                 parentCompoundUuidList: List[HierarchyCompoundUuid]): List[ParentTopic] =
    topicEntity.getAllParents.asScala.toList
      .map(topic => {
        val uuid         = topic.getUuid
        val virtualName  = parentCompoundUuidList.find(_.uuid == uuid).flatMap(_.name)
        val compoundUuid = HierarchyCompoundUuid(uuid, virtualName).toString(false)
        val topicName    = buildVirtualTopicText(topic.getName, virtualName)
        ParentTopic(compoundUuid, topicName)
      })

  /**
    * Get all key resources for the given topic entity.
    *
    * @param topicEntity The topic entity used to get key resources.
    * @param topicCompoundUuid The compound uuid of the given topic entity.
    */
  def getAllKeyResources(topicEntity: HierarchyTopicEntity,
                         topicCompoundUuid: String): List[SearchResultItem] = {
    // Get dynamic key resources item.
    // Workflow: HierarchyTopicDynamicKeyResources -> ItemId -> Item
    def getDynamicKeyResources(compoundUuids: String): List[Item] = {
      val legacyCompoundUuid = HierarchyCompoundUuid(compoundUuids).toString(true)
      val dynamicKeyResources = Option(hierarchyService.getDynamicKeyResource(legacyCompoundUuid))
        .map(_.asScala.toList)
        .getOrElse(List.empty)
      val dynamicKeyResourcesItemIds =
        dynamicKeyResources.map(resources => new ItemId(resources.getUuid, resources.getVersion))
      val dynamicKeyResourcesItems =
        dynamicKeyResourcesItemIds.flatMap(id => Option(itemService.getUnsecureIfExists(id)))

      dynamicKeyResourcesItems
    }

    // get key resources and convert Item to SearchResultItem
    val keyResourceItems = topicEntity.getKeyResources.asScala.toList
    val allItems = convertKeyResourceItems(
      getDynamicKeyResources(topicCompoundUuid) ++ keyResourceItems)

    allItems
  }
}
