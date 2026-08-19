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

package io.github.openequella.graphql.api.views

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.client.{OtherTargetList, OtherTargetListType}

/** View model for a sub-entity access control list, keyed by a discriminated target type.
  *
  * @param targetType
  *   Discriminator indicating what kind of target this list is for.
  * @param itemStatus
  *   For ITEM_STATUS targets: the item status (e.g. DRAFT, LIVE). None for other types.
  * @param metadataRuleId
  *   For ITEM_METADATA targets: the metadata rule ID. None for other types.
  * @param taskId
  *   For WORKFLOW_TASK targets: the workflow task ID. None for other types.
  * @param entries
  *   The access control entries for this target.
  */
final case class OtherTargetListView(
    targetType: OtherTargetListType,
    itemStatus: Option[String],
    metadataRuleId: Option[String],
    taskId: Option[String],
    entries: List[TargetListEntryView]
)

object OtherTargetListView {
  val selector: SelectionBuilder[OtherTargetList, OtherTargetListView] =
    (
      OtherTargetList.targetType ~
        OtherTargetList.itemStatus ~
        OtherTargetList.metadataRuleId ~
        OtherTargetList.taskId ~
        OtherTargetList.entries(TargetListEntryView.selector)
    ).mapN(OtherTargetListView.apply _)
}
