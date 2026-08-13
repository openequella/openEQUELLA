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

package com.tle.web.remoting.graphql

import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.beans.entity.{BaseEntity, Schema}
import com.tle.beans.item.ItemStatus
import com.tle.common.EntityPack
import com.tle.common.security.{
  ItemMetadataTarget,
  ItemStatusTarget,
  TargetList,
  TargetListEntry,
  WorkflowTaskTarget
}

import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

/** Builders and constants for the legacy security structures (`TargetListEntry`, `TargetList` and
  * `EntityPack`) which the GraphQL security types are converted from. Shared by the tests of those
  * types and of the providers which produce them.
  *
  * Only the primitives live here - scenario specific arrangements (e.g. "the target lists a
  * collection produces") belong with the test that gives them meaning.
  */
object SecurityTestFixtures {

  // Privileges
  val EditSchemaPrivilege   = "EDIT_SCHEMA"
  val ViewItemPrivilege     = "VIEW_ITEM"
  val EditItemPrivilege     = "EDIT_ITEM"
  val ModerateItemPrivilege = "MODERATE_ITEM"

  // 'Who' expressions
  val TestUser       = "U:testuser"
  val EditorsGroup   = "G:editors"
  val ModeratorsRole = "R:moderators"

  // Identifiers of sub-entity targets
  val MetadataRuleId = "rule-uuid-123"
  val WorkflowId     = 42L
  val WorkflowTaskId = "task-uuid-456"

  /** An entry for the given privilege and subject - granted and non-overriding unless stated. */
  def targetListEntry(
      privilege: String,
      who: String,
      granted: Boolean = true,
      overridden: Boolean = false
  ): TargetListEntry =
    new TargetListEntry().tap { entry =>
      entry.setGranted(granted)
      entry.setOverride(overridden)
      entry.setPrivilege(privilege)
      entry.setWho(who)
    }

  /** A target list holding a single entry for the given privilege and subject. */
  def targetList(
      privilege: String,
      who: String,
      granted: Boolean = true,
      overridden: Boolean = false
  ): TargetList =
    new TargetList(java.util.List.of(targetListEntry(privilege, who, granted, overridden)))

  /** A target list which is present but carries no entries. */
  def emptyTargetList: TargetList =
    new TargetList(java.util.Collections.emptyList[TargetListEntry]())

  /** The owning collection of the item-scoped targets below. Its identity is what matters to those
    * targets, not its contents.
    */
  val ItemDef = new ItemDefinition()

  def itemStatusTarget(status: ItemStatus): ItemStatusTarget = new ItemStatusTarget(status, ItemDef)

  def itemMetadataTarget: ItemMetadataTarget = new ItemMetadataTarget(MetadataRuleId, ItemDef)

  def workflowTaskTarget: WorkflowTaskTarget = new WorkflowTaskTarget(WorkflowId, WorkflowTaskId)

  /** Assembles the legacy `Map` keyed by sub-entity target which `ImportExportPack` carries. */
  def otherTargetListsOf(
      entries: (Object, TargetList)*
  ): java.util.Map[Object, TargetList] =
    new java.util.HashMap[Object, TargetList](entries.toMap.asJava)

  /** A pack for an arbitrary entity, with the given target lists - either of which may be null, as
    * they are for an entity with no ACLs of that kind. Typed to `BaseEntity` because the entity
    * type agnostic paths under test deal in the supertype; the entity itself is incidental here.
    */
  def entityPack(
      targetList: TargetList = null,
      otherTargetLists: java.util.Map[Object, TargetList] = null
  ): EntityPack[BaseEntity] =
    new EntityPack[BaseEntity]().tap { pack =>
      pack.setEntity(new Schema())
      pack.setTargetList(targetList)
      pack.setOtherTargetLists(otherTargetLists)
    }
}
