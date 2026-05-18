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

package com.tle.web.remoting.graphql.schema.types

import caliban.schema.Annotations.GQLDescription
import com.tle.common.security.{ItemMetadataTarget, ItemStatusTarget, WorkflowTaskTarget}

/** Discriminator enum for the different kinds of sub-entity target lists. Rendered as a GraphQL
  * enum, which is valid for both input and output types.
  */
@GQLDescription("The type of sub-entity target for an access control list.")
sealed trait OtherTargetListType
object OtherTargetListType {
  @GQLDescription("Per-item-status ACLs (e.g. DRAFT, LIVE, MODERATING).")
  case object ITEM_STATUS extends OtherTargetListType
  @GQLDescription("Per-metadata-rule ACLs.")
  case object ITEM_METADATA extends OtherTargetListType
  @GQLDescription("Per-workflow-task ACLs.")
  case object WORKFLOW_TASK extends OtherTargetListType
}

/** A sub-entity access control list, keyed by a discriminated target type. Uses a tagged
  * representation (case class + enum discriminator) rather than a GraphQL union so that it is valid
  * as both an output type and an input type.
  *
  * Exactly one of `itemStatus`, `metadataRuleId`, or `taskId` will be set, matching the
  * `targetType` discriminator.
  */
@GQLDescription(
  "A sub-entity access control list, keyed by the type of target (e.g. item status, metadata rule, workflow task)."
)
final case class OtherTargetList(
    @GQLDescription("Discriminator indicating what kind of target this list is for.")
    targetType: OtherTargetListType,
    @GQLDescription(
      "For ITEM_STATUS targets: the item status this list applies to (e.g. DRAFT, LIVE, MODERATING). Null for other target types."
    )
    itemStatus: Option[String] = None,
    @GQLDescription(
      "For ITEM_METADATA targets: the ID of the metadata rule this list applies to. Null for other target types."
    )
    metadataRuleId: Option[String] = None,
    @GQLDescription(
      "For WORKFLOW_TASK targets: the ID of the workflow task this list applies to. Null for other target types."
    )
    taskId: Option[String] = None,
    @GQLDescription("The access control entries for this target.")
    entries: List[TargetListEntry]
)

object OtherTargetList {

  import scala.jdk.CollectionConverters._

  /** Converts the legacy `Map[Object, TargetList]` from `ImportExportPack` into a typed list of
    * `OtherTargetList` entries.
    *
    * @param otherTargetLists
    *   The map from the entity pack (may be null).
    * @return
    *   A list of typed `OtherTargetList` entries, or empty if the map is null/empty.
    */
  def fromMap(
      otherTargetLists: java.util.Map[Object, com.tle.common.security.TargetList]
  ): List[OtherTargetList] =
    Option(otherTargetLists)
      .map(_.asScala.toList)
      .getOrElse(List.empty)
      .map((convertEntry _).tupled)

  /** Converts a single key-value pair from the legacy map into a typed `OtherTargetList`. Pattern
    * matches on the key type to determine the discriminator and key field.
    */
  private def convertEntry(
      key: Object,
      targetList: com.tle.common.security.TargetList
  ): OtherTargetList = {
    val entries = TargetListEntry.fromTargetList(targetList)
    val base    = keyToBase(key)
    base.copy(entries = entries)
  }

  /** Extracts the discriminator and identifier from a legacy target key, returning an
    * `OtherTargetList` with the appropriate `targetType` and key field set (but no entries).
    */
  private def keyToBase(key: Object): OtherTargetList = key match {
    case ist: ItemStatusTarget =>
      OtherTargetList(
        targetType = OtherTargetListType.ITEM_STATUS,
        itemStatus = Some(getMandatoryItemStatus(ist)),
        entries = List.empty
      )
    case imt: ItemMetadataTarget =>
      OtherTargetList(
        targetType = OtherTargetListType.ITEM_METADATA,
        metadataRuleId = Some(imt.getId),
        entries = List.empty
      )
    case wtt: WorkflowTaskTarget =>
      OtherTargetList(
        targetType = OtherTargetListType.WORKFLOW_TASK,
        taskId = Some(wtt.getTaskId),
        entries = List.empty
      )
    case other =>
      throw new IllegalArgumentException(
        s"Unsupported otherTargetLists key type: ${other.getClass.getName}"
      )
  }

  /** Extracts the mandatory item status string from an `ItemStatusTarget`, throwing an exception if
    * it is missing. This is used for validating input keys when converting back to the legacy map
    * format, where the item status string is required for lookups.
    */
  private def getMandatoryItemStatus(ist: ItemStatusTarget): String = {
    val itemStatus = for {
      statusObj <- Option(ist.getItemStatus)
      statusStr <- Option(statusObj.exactString)
    } yield statusStr

    itemStatus.getOrElse {
      throw new IllegalArgumentException(
        "ItemStatusTarget key is missing mandatory item status."
      )
    }
  }
}
