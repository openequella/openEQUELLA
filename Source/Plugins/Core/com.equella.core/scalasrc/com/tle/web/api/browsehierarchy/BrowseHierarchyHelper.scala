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

import cats.Semigroup
import cats.implicits.catsSyntaxSemigroup
import com.tle.beans.entity.LanguageBundle
import com.tle.beans.hierarchy.{HierarchyTopic => HierarchyTopicEntity}
import com.tle.beans.item.{Item, ItemId, ItemIdKey}
import com.tle.common.URLUtils
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
      parentCompoundUuidMap: Option[Map[String, String]]): HierarchyTopicSummary = {
    val uuid            = topic.getUuid
    val collectionUuids = hierarchyService.getCollectionUuids(topic)
    val currentTopicMap = virtualTopicName.map(t => Map(uuid -> t))

    // This custom semigroup is used when merging the current topic UUID map into the parent topic UUID map.
    // If the parent map accidentally has key pairs that also exist in the current topic UUID map,
    // the values should be ALWAYS replace by the values of the current topic map.
    // As a result, the combine function ALWAYS return `currentTopicName`.
    implicit val topicNameSemigroup: Semigroup[String] = Semigroup.instance(
      (_, currentTopicName) => currentTopicName
    )
    val compoundUuidMap     = parentCompoundUuidMap |+| currentTopicMap
    val compoundUuidJavaMap = compoundUuidMap.getOrElse(Map.empty).asJava

    // Normal sub topics and sub virtual topics.
    val subTopics = hierarchyService
      .expandVirtualisedTopics(hierarchyService.getSubTopics(topic),
                               compoundUuidJavaMap,
                               collectionUuids.orElse(null))
      .asScala
      .toList
      .map(buildHierarchyTopicSummary(_, compoundUuidMap))

    HierarchyTopicSummary(
      // It should return the compound uuid with parent compound uuid(virtual parent).
      buildCompoundUuid(uuid, virtualTopicName, parentCompoundUuidMap),
      hierarchyService.getMatchingItemCount(topic, compoundUuidJavaMap),
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
    * @param parentCompoundUuidMap A map contains the given topic's virtual ancestors (all virtual parents),
    *                              which key is the topic uuid and value is the virtual topic name.
    */
  def buildHierarchyTopicSummary(
      topicWrapper: VirtualisableAndValue[HierarchyTopicEntity],
      parentCompoundUuidMap: Option[Map[String, String]] = None): HierarchyTopicSummary = {
    val topic: HierarchyTopicEntity = topicWrapper.getVt
    val matchedVirtualText          = Option(topicWrapper.getVirtualisedValue)
    buildHierarchyTopicSummary(topic, matchedVirtualText, parentCompoundUuidMap)
  }

  // Build a compound uuid based on topic uuid and topic name and it's parent compound uuid map
  private def buildCompoundUuid(
      uuid: String,
      name: Option[String],
      parentCompoundUuidMap: Option[Map[String, String]] = None): String = {
    val currentCompoundUuid = name.map(n => s"$uuid:$n").getOrElse(uuid)

    parentCompoundUuidMap
      .map(
        _.foldLeft(currentCompoundUuid) {
          case (compoundUuid, (uuid, name)) =>
            s"$compoundUuid,${buildCompoundUuid(uuid, Option(name))}"
        }
      )
      .getOrElse(currentCompoundUuid)
  }

  /**
    * Given a compound UUID, return a tuple where the first value is UUID and the second value is virtual topic name.
    */
  def getUuidAndName(compoundUuid: String): (String, Option[String]) =
    compoundUuid.split(":", 2) match {
      case Array(uuid, name) => (uuid, Option(name))
      case Array(uuid)       => (uuid, None)
    }

  /**
    * If the given compoundUuid contains virtual topic name build a map where key is topic UUID and value is virtual topic name.
    * If the given compoundUuid does not contain virtual topic name, return an empty map.
    * Notice the name is not encoded because it will be used in Luence query to search related item.
    *
    * @param compoundUuid A compound UUID.
    */
  def buildCompoundUuidMap(compoundUuid: String): Map[String, String] =
    getUuidAndName(compoundUuid) match {
      case (uuid, Some(name)) => Map(uuid -> name)
      case _                  => Map.empty
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
    * Encode name in the compound ID for virtual topics.
    * Encoded compound UUID is used to look up dynamic resources, because the UUID stored in database is encoded.
    *
    * For example:
    * Input:
    * "46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:A James"
    * Output:
    * "46249813-019d-4d14-b772-2a8ca0120c99:Hobart,886aa61d-f8df-4e82-8984-c487849f80ff:A+James"
    */
  def encodeCompoundUuid(compoundUuid: String): String =
    compoundUuid
      .split(",")
      .map(getUuidAndName)
      .map {
        case (uuid, name) => buildCompoundUuid(uuid, name.map(URLUtils.basicUrlEncode))
      }
      .mkString(",")

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
    * @param parentCompoundUuidMap A map contains the given topic's virtual ancestors (all virtual parents) info.
    */
  def getTopicSummary(topicEntity: HierarchyTopicEntity,
                      virtualTopicName: Option[String],
                      parentCompoundUuidMap: Map[String, String]): HierarchyTopicSummary = {
    val wrappedTopic: VirtualisableAndValue[HierarchyTopicEntity] =
      wrapHierarchyTopic(topicEntity, virtualTopicName)
    buildHierarchyTopicSummary(wrappedTopic, Option(parentCompoundUuidMap))
  }

  /**
    * Get all parents' compound uuid and topic Name.
    *
    * @param topicEntity The topic entity used to get parents.
    * @param parentCompoundUuidMap A map contains the given topic's virtual ancestors (all virtual parents) info.
    */
  def getParents(topicEntity: HierarchyTopicEntity,
                 parentCompoundUuidMap: Map[String, String]): List[ParentTopic] =
    topicEntity.getAllParents.asScala.toList
      .map(topic => {
        val uuid         = topic.getUuid
        val virtualName  = parentCompoundUuidMap.get(uuid)
        val compoundUuid = buildCompoundUuid(uuid, virtualName)
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
      val encodedId = encodeCompoundUuid(compoundUuids)
      val dynamicKeyResources = Option(hierarchyService.getDynamicKeyResource(encodedId))
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
