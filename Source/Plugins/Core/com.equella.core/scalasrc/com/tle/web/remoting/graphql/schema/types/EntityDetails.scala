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

import java.time.LocalDateTime
import scala.jdk.CollectionConverters._

/** Represents the details of an entity in the system, providing essential metadata such as
  * identifiers, ownership, timestamps, and additional attributes. In the java Entity structure,
  * this is the BaseEntity, which is the parent of all entities. However being in Scala and more of
  * a functional programming style, we don't want an inheritance hierarchy, instead we use
  * composition to represent the details of an entity. This also allows us to use the Caliban
  * GraphQL annotations directly on the fields, making it easier to document.
  */
@GQLDescription(
  "Common details for entities, including identifiers, ownership, timestamps, and additional attributes."
)
final case class EntityDetails(
    @GQLDescription("Unique identifier for this entity.")
    id: Long,
    @GQLDescription(
      "Universally unique identifier for this entity, used for referencing across different entities."
    )
    uuid: String,
    @GQLDescription("Owner of this entity, typically the user who created it.")
    owner: String,
    @GQLDescription(
      "Timestamp for when this entity was created - only valid when starting an edit on an existing entity."
    )
    dateCreated: Option[LocalDateTime],
    @GQLDescription(
      "Timestamp for when this entity was last modified - only valid when starting an edit on an existing entity."
    )
    dateModified: Option[LocalDateTime],
    @GQLDescription(
      "Language bundle ID for the _name_ of this entity, used for internationalization."
    )
    nameBundleId: Option[Long],
    @GQLDescription(
      "Language bundle ID for the _description_ of this entity, used for internationalization."
    )
    descriptionBundleId: Option[Long],
    @GQLDescription(
      "A map of additional attributes for this entity, allowing for flexible metadata storage."
    )
    attributes: Map[String, String],
    @GQLDescription(
      "Indicates whether this entity is disabled, affecting its visibility and usability."
    )
    disabled: Boolean
)
object EntityDetails {
  def apply(uuid: String, owner: String): EntityDetails = EntityDetails(
    id = 0L,
    uuid = uuid,
    owner = owner,
    dateCreated = null,
    dateModified = null,
    nameBundleId = None,
    descriptionBundleId = None,
    attributes = Map.empty,
    disabled = false
  )

  /** Converts a BaseEntity into EntityDetails, extracting relevant fields and converting types as
    * necessary. This is typically used when fetching entity details from the database.
    *
    * @param entity
    *   the BaseEntity to convert
    * @return
    *   an EntityDetails instance populated with the entity's data
    */
  def apply(entity: com.tle.beans.entity.BaseEntity): EntityDetails = EntityDetails(
    id = entity.getId,
    uuid = entity.getUuid,
    owner = entity.getOwner,
    dateCreated = toLocalDateTime(entity.getDateCreated),
    dateModified = toLocalDateTime(entity.getDateModified),
    nameBundleId = languageBundleId(entity.getName),
    descriptionBundleId = languageBundleId(entity.getDescription),
    attributes = entity.getAttributes.asScala.toMap,
    disabled = entity.isDisabled
  )
}
