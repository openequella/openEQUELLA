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

import com.tle.admin.graphql.conversion.LanguageBundleViewConverter.fromLanguageBundle
import com.tle.beans.entity.BaseEntity
import io.github.openequella.graphql.api.views.EntityDetailsView

import scala.jdk.CollectionConverters._

/** Converter for [[EntityDetailsView]] to/from [[BaseEntity]].
  *
  * This converter handles the common entity fields that are shared across all entity types,
  * including identifiers, ownership, language bundles, attributes, and disabled state.
  */
object EntityDetailsViewConverter {

  /** Applies all common fields from an [[EntityDetailsView]] to a [[BaseEntity]] in-place.
    *
    * This is the reverse of [[fromBaseEntity]] and is intended to be used when constructing a
    * `BaseEntity` subclass from a GraphQL view. It sets all fields that are common to all entity
    * types, allowing entity-specific converters to focus only on their unique fields.
    *
    * @param entity
    *   the BaseEntity to populate
    * @param view
    *   the EntityDetailsView containing the source data
    */
  def applyToBaseEntity(entity: BaseEntity, view: EntityDetailsView): Unit = {
    entity.setId(view.id)
    entity.setUuid(view.uuid)
    entity.setOwner(view.owner)

    view.dateCreated.foreach(d => entity.setDateCreated(toDate(d)))
    view.dateModified.foreach(d => entity.setDateModified(toDate(d)))

    EntityDetailsViewStrings(view).applyTo(entity)

    entity.setAttributes(view.attributes.asJava)

    entity.setDisabled(view.disabled)
  }

  /** Converts a [[BaseEntity]] to [[EntityDetailsView]], extracting relevant fields and converting
    * types as necessary.
    *
    * @param entity
    *   the BaseEntity to convert
    * @return
    *   an EntityDetailsView instance populated with the entity's data
    */
  def fromBaseEntity(entity: BaseEntity): EntityDetailsView = EntityDetailsView(
    id = entity.getId,
    uuid = entity.getUuid,
    owner = entity.getOwner,
    dateCreated = toLocalDateTime(entity.getDateCreated),
    dateModified = toLocalDateTime(entity.getDateModified),
    nameBundle = Option(entity.getName).map(fromLanguageBundle),
    descriptionBundle = Option(entity.getDescription).map(fromLanguageBundle),
    attributes = entity.getAttributes.asScala.toMap,
    disabled = entity.isDisabled
  )
}
