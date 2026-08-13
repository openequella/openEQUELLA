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

import com.tle.admin.graphql.conversion.LanguageBundleViewConverter.toLanguageBundle
import com.tle.beans.entity.{BaseEntity, LanguageBundle}
import io.github.openequella.graphql.api.views.EntityDetailsView

/** A simple case class to hold the name and description LanguageBundles from an EntityDetailsView.
  * Both fields are optional, as not all entity types have both name and description.
  *
  * @param name
  *   Optional LanguageBundle for the entity's name.
  * @param description
  *   Optional LanguageBundle for the entity's description.
  */
final case class EntityDetailsViewStrings(
    name: Option[LanguageBundle],
    description: Option[LanguageBundle]
) {

  /** Applies the language strings to the given [[BaseEntity]], setting its name and description.
    *
    * @param entity
    *   the BaseEntity to update
    */
  def applyTo(entity: BaseEntity): Unit = {
    entity.setName(name.orNull)
    entity.setDescription(description.orNull)
  }
}

object EntityDetailsViewStrings {

  /** EntityDetailsView contain two fundamental language bundles - name and description. This method
    * is a convenience method to convert these two bundles into LanguageBundle instances. Although
    * simple to do, this conversion is frequently needed for many entity types. Thereby centralising
    * the conversion here, keeps things DRY.
    */
  def apply(view: EntityDetailsView): EntityDetailsViewStrings =
    EntityDetailsViewStrings(
      view.nameBundle.map(toLanguageBundle),
      view.descriptionBundle.map(toLanguageBundle)
    )
}
