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
import com.tle.common.EntityPack

import scala.jdk.CollectionConverters._

/** Based on EntityPack with a focus on what's needed for GraphQL interactions.
  *
  * TODO: ImportExportPack (super class of EntityPack) also includes a `otherTargetLists`, but for
  * now we are not using it. It adds some complexity seeing it is a map with `Object` keys. Will
  * need to address this in the future if we need to support it. (Possibly for ACL lists for items
  * etc. but so far the entities we are dealing with in GraphQL do not have this.)
  */
@GQLDescription(
  "The details for an entity that is now ready to be edited, or the updated details for that entity after editing."
)
final case class EditableEntity[T](
    @GQLDescription("Base entity being edited")
    entity: T,
    @GQLDescription(
      "The ID of the staging area for this entity. This is used to store files before they are committed."
    )
    stagingId: String,
    @GQLDescription(
      "The version of openEQUELLA this entity is being edited against. This is used to ensure that edits are applied to the correct version."
    )
    version: Option[String],
    @GQLDescription(
      "A list of access control entries, each specifying a privilege granted to a user or group, " +
        "along with whether it is overridden or granted."
    )
    targetList: List[TargetListEntry]
)
object EditableEntity {

  /** Converts an EntityPack to an EditableEntity. That is, converts from the old way of
    * transferring details of an Entity for editing to the new way of using GraphQL.
    *
    * @param pack
    *   The EntityPack containing the entity and its metadata.
    * @param convertEntity
    *   A function to convert the entity from the pack to the desired type T. Typically the apply of
    *   a companion object.
    * @tparam E
    *   The type of the entity in the EntityPack.
    * @tparam T
    *   The type of the entity in the resulting EditableEntity.
    * @return
    *   An EditableEntity containing the converted entity and its metadata.
    */
  def apply[E <: com.tle.beans.entity.BaseEntity, T](
      pack: EntityPack[E],
      convertEntity: E => T
  ): EditableEntity[T] = {
    EditableEntity(
      entity = convertEntity(pack.getEntity),
      stagingId = pack.getStagingID,
      version = Option(pack.getVersion),
      targetList = convertTargetList(pack.getTargetList)
    )
  }

  private def convertTargetList(
      targetList: com.tle.common.security.TargetList
  ): List[TargetListEntry] = {
    targetList.getEntries.asScala.toList.map(TargetListEntry(_))
  }
}
