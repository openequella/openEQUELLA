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

package com.tle.admin.graphql.conversion

import com.tle.admin.graphql.conversion.TargetListConverter.{fromTargetList, toTargetList}
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.common.security.{
  ItemMetadataTarget,
  ItemStatusTarget,
  TargetList,
  WorkflowTaskTarget
}
import io.github.openequella.graphql.api.views.OtherTargetListView
import io.github.openequella.graphql.client.OtherTargetListType

import scala.jdk.CollectionConverters._

/** Converts between the legacy `Map[Object, TargetList]` from `EntityPack.getOtherTargetLists()`
  * and the typed `List[OtherTargetListView]` used in the GraphQL view models.
  */
object OtherTargetListViewConverter {

  /** Converts the legacy `Map[Object, TargetList]` into a list of `OtherTargetListView`.
    *
    * @param otherTargetLists
    *   The map from the entity pack (may be null).
    * @return
    *   A list of typed view models, or empty if the map is null/empty.
    */
  def fromTargetListMap(
      otherTargetLists: java.util.Map[Object, TargetList]
  ): List[OtherTargetListView] =
    Option(otherTargetLists)
      .map(_.asScala.toList.map((convertEntry _).tupled))
      .getOrElse(List.empty)

  /** Converts a list of `OtherTargetListView` back to the legacy `Map[Object, TargetList]`.
    *
    * @param views
    *   The list of view models to convert.
    * @param itemDefinition
    *   The `ItemDefinition` (Collection) being edited. Required as a contextual back-reference for
    *   `ItemStatusTarget` and `ItemMetadataTarget` keys — see the Javadoc on those classes for why
    *   this matters for `equals()` lookups.
    * @return
    *   A Java map suitable for `EntityPack.setOtherTargetLists()`, or null if the list is empty.
    */
  def toTargetListMap(
      views: List[OtherTargetListView],
      itemDefinition: ItemDefinition
  ): java.util.Map[Object, TargetList] =
    if (views.isEmpty) null
    else views.map(viewToEntry(_, itemDefinition)).toMap.asJava

  private def convertEntry(key: Object, targetList: TargetList): OtherTargetListView = {
    val entries = targetList convert fromTargetList
    val base    = keyToBase(key)
    base.copy(entries = entries)
  }

  private def keyToBase(key: Object): OtherTargetListView = key match {
    case ist: ItemStatusTarget =>
      OtherTargetListView(
        targetType = OtherTargetListType.ITEM_STATUS,
        itemStatus = Some(ist.getItemStatus.exactString),
        metadataRuleId = None,
        taskId = None,
        entries = List.empty
      )
    case imt: ItemMetadataTarget =>
      OtherTargetListView(
        targetType = OtherTargetListType.ITEM_METADATA,
        itemStatus = None,
        metadataRuleId = Some(imt.getId),
        taskId = None,
        entries = List.empty
      )
    case wtt: WorkflowTaskTarget =>
      OtherTargetListView(
        targetType = OtherTargetListType.WORKFLOW_TASK,
        itemStatus = None,
        metadataRuleId = None,
        taskId = Some(wtt.getTaskId),
        entries = List.empty
      )
    case other =>
      throw new IllegalArgumentException(
        s"Unsupported otherTargetLists key type: ${other.getClass.getName}"
      )
  }

  private def viewToEntry(
      view: OtherTargetListView,
      itemDefinition: ItemDefinition
  ): (Object, TargetList) = {
    val key: Object = view.targetType match {
      case OtherTargetListType.ITEM_STATUS =>
        val statusName = view.itemStatus.getOrElse(
          throw new IllegalArgumentException("ITEM_STATUS target missing itemStatus")
        )
        val itemStatus = com.tle.beans.item.ItemStatus.valueOf(statusName)
        new ItemStatusTarget(itemStatus, itemDefinition)
      case OtherTargetListType.ITEM_METADATA =>
        val ruleId = view.metadataRuleId.getOrElse(
          throw new IllegalArgumentException("ITEM_METADATA target missing metadataRuleId")
        )
        new ItemMetadataTarget(ruleId, itemDefinition)
      case OtherTargetListType.WORKFLOW_TASK =>
        val taskId = view.taskId.getOrElse(
          throw new IllegalArgumentException("WORKFLOW_TASK target missing taskId")
        )
        new WorkflowTaskTarget(0L, taskId)
    }
    key -> (view.entries convert toTargetList)
  }
}
