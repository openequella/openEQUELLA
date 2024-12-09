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
import com.tle.beans.hierarchy.{HierarchyTopicKeyResource, HierarchyTopic => HierarchyTopicEntity}
import com.tle.beans.item.{Item, ItemId, ItemIdKey}
import com.tle.common.interfaces.equella.BundleString
import com.tle.common.security.Privilege
import com.tle.core.guice.Bind
import com.tle.core.hierarchy.HierarchyService
import com.tle.core.item.serializer.{ItemSerializerItemBean, ItemSerializerService}
import com.tle.core.item.service.ItemService
import com.tle.core.search.VirtualisableAndValue
import com.tle.core.security.TLEAclManager
import com.tle.web.api.browsehierarchy.model.{HierarchyTopicSummary, KeyResource, ParentTopic}
import com.tle.web.api.search.SearchHelper
import com.tle.web.api.search.model.SearchItem

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Bind
@Singleton
class BrowseHierarchyHelper {
  private var hierarchyService: HierarchyService           = _
  private var itemSerializerService: ItemSerializerService = _
  private var itemService: ItemService                     = _
  private var aclManager: TLEAclManager                    = _

  // An intermediate class to store the item and its version information.
  private case class KeyResourceItem(item: Item, isLatest: Boolean)

  @Inject
  def this(
      hierarchyService: HierarchyService,
      itemService: ItemService,
      itemSerializerService: ItemSerializerService,
      aclManager: TLEAclManager
  ) = {
    this()
    this.hierarchyService = hierarchyService
    this.itemService = itemService
    this.itemSerializerService = itemSerializerService
    this.aclManager = aclManager
  }

  /** Get the localized string for the given bundle and if virtual text is existing, replace the
    * placeholder %s with matched virtual text.
    */
  private def buildVirtualTopicText(
      bundle: LanguageBundle,
      matchedVirtualText: Option[String]
  ): Option[String] = {
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
      parentCompoundUuidList: Option[List[HierarchyCompoundUuid]]
  ): HierarchyTopicSummary = {
    val uuid            = topic.getUuid
    val collectionUuids = hierarchyService.getCollectionUuids(topic)
    val currentTopic    = HierarchyCompoundUuid(uuid, virtualTopicName, parentCompoundUuidList)

    val parentUuidListForSubTopics    = currentTopic.getAllVirtualHierarchyList
    val parentUuidJavaMapForSubTopics = currentTopic.getAllVirtualHierarchyMap.asJava
    // Normal sub topics and sub virtual topics.
    val subTopics = hierarchyService
      .expandVirtualisedTopics(
        hierarchyService.getSubTopics(topic),
        parentUuidJavaMapForSubTopics,
        collectionUuids.orElse(null)
      )
      .asScala
      .toList
      .map(buildHierarchyTopicSummary(_, Option(parentUuidListForSubTopics)))

    HierarchyTopicSummary(
      // The string representation of the topic compound UUID.
      currentTopic.buildString(),
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

  /** Constructs a HierarchyTopic instance with the given topic and its parent's compound uuid map.
    *
    * @param topicWrapper
    *   The wrapper object encapsulating the topic entity to be converted.
    * @param parentCompoundUuidList
    *   A List of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual
    *   parents).
    */
  def buildHierarchyTopicSummary(
      topicWrapper: VirtualisableAndValue[HierarchyTopicEntity],
      parentCompoundUuidList: Option[List[HierarchyCompoundUuid]] = None
  ): HierarchyTopicSummary = {
    val topic: HierarchyTopicEntity = topicWrapper.getVt
    val matchedVirtualText          = Option(topicWrapper.getVirtualisedValue)
    buildHierarchyTopicSummary(topic, matchedVirtualText, parentCompoundUuidList)
  }

  /** Wrap the given topic entity into a VirtualisableAndValue object.
    *
    * @param topic
    *   The topic entity to be wrapped.
    * @param virtualTopicName
    *   The virtual topic name for the given topic.
    */
  private def wrapHierarchyTopic(
      topic: HierarchyTopicEntity,
      virtualTopicName: Option[String]
  ): VirtualisableAndValue[HierarchyTopicEntity] = {
    virtualTopicName
      // No need to worry about count, it won't be used in new endpoint.
      .map(name => new VirtualisableAndValue(topic, name, 0))
      .getOrElse(new VirtualisableAndValue(topic))
  }

  /** Get the topic summary for the given topic entity.
    *
    * @param topicEntity
    *   The topic entity used to get summary.
    * @param virtualTopicName
    *   The virtual topic name for the given topic.
    * @param parentCompoundUuidList
    *   A list of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual
    *   parents).
    */
  def getTopicSummary(
      topicEntity: HierarchyTopicEntity,
      virtualTopicName: Option[String],
      parentCompoundUuidList: Option[List[HierarchyCompoundUuid]]
  ): HierarchyTopicSummary = {
    val wrappedTopic: VirtualisableAndValue[HierarchyTopicEntity] =
      wrapHierarchyTopic(topicEntity, virtualTopicName)
    buildHierarchyTopicSummary(wrappedTopic, parentCompoundUuidList)
  }

  /** Get all parents' compound uuid and topic Name.
    *
    * @param topicEntity
    *   The topic entity used to get parents.
    * @param parentCompoundUuidList
    *   A list of HierarchyCompoundUuid for the given topic's virtual ancestors (all virtual
    *   parents).
    */
  def getParents(
      topicEntity: HierarchyTopicEntity,
      parentCompoundUuidList: List[HierarchyCompoundUuid]
  ): List[ParentTopic] =
    topicEntity.getAllParents.asScala.toList
      .map(topic => {
        val uuid         = topic.getUuid
        val virtualName  = parentCompoundUuidList.find(_.uuid == uuid).flatMap(_.name)
        val compoundUuid = HierarchyCompoundUuid(uuid, virtualName).buildString()
        val topicName    = buildVirtualTopicText(topic.getName, virtualName)
        ParentTopic(compoundUuid, topicName)
      })

  // Convert HierarchyTopicKeyResource entity to KeyResourceItem and
  // filter out the key resources which users do not have the privilege to access.
  private def convertToKeyResourceItem(
      keyResourceEntity: HierarchyTopicKeyResource
  ): Option[KeyResourceItem] = {
    val version = keyResourceEntity.getItemVersion
    val uuid    = keyResourceEntity.getItemUuid

    val realVersion = itemService.getRealVersion(version, uuid)
    val realItemId  = new ItemId(uuid, realVersion)

    for {
      item <- Option(itemService.getUnsecureIfExists(realItemId))
      if aclManager.hasPrivilege(item, Privilege.DISCOVER_ITEM)
    } yield KeyResourceItem(item, version == 0)
  }

  // Convert KeyResourceItem to KeyResource.
  private def convertToKeyResource(
      keyResourceItem: KeyResourceItem,
      serializer: ItemSerializerItemBean
  ): KeyResource = {
    val realItemIdKey = new ItemIdKey(keyResourceItem.item)
    val searchItem    = SearchItem(realItemIdKey, isKeywordFoundInAttachment = false, serializer)
    KeyResource(SearchHelper.convertToItem(searchItem), keyResourceItem.isLatest)
  }

  /** Return details of all the key resources for the given topic ID by following steps:
    *
    *   1. Get the list of raw key resource by the given ID.
    *
    * 2. For each raw key resource, find the real version of the referenced item.
    *
    * 3. Get the referenced Items with permission check.
    *
    * 4. Generate key resource details based on the reference Items.
    *
    * @param topicCompoundUuid
    *   The compound uuid of the given topic entity.
    */
  def getKeyResources(topicCompoundUuid: HierarchyCompoundUuid): List[KeyResource] = {
    val keyResourceItems = hierarchyService
      .getKeyResources(topicCompoundUuid)
      .asScala
      .toList
      .flatMap(convertToKeyResourceItem)

    val itemIdKeys = keyResourceItems.map(_.item).map(new ItemIdKey(_))
    val serializer = SearchHelper.createSerializer(itemIdKeys)

    keyResourceItems.map(convertToKeyResource(_, serializer))
  }
}
