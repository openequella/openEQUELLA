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

package com.tle.web.remoting.graphql.schema.conversion

import com.tle.beans.entity.BaseEntity
import com.tle.web.remoting.graphql.schema.types.{EntityDetails, LanguageBundle, LanguageString}

import scala.jdk.CollectionConverters._

/** Converter for populating [[BaseEntity]] fields from [[EntityDetails]].
  *
  * This converter handles the common entity fields that are shared across all entity types,
  * including identifiers, ownership, language bundles, attributes, and disabled state.
  *
  * Note: Date fields (dateCreated, dateModified) are intentionally not set as they are managed
  * internally by the system.
  */
object EntityDetailsConverter {

  /** Populates the common [[BaseEntity]] fields from the provided [[EntityDetails]].
    *
    * @param details
    *   the GraphQL entity details containing the field values
    * @param entity
    *   the target entity to populate
    */
  def populateBaseEntity(details: EntityDetails, entity: BaseEntity): Unit = {
    entity.setId(details.id)
    entity.setUuid(details.uuid)
    entity.setOwner(details.owner)
    entity.setDisabled(details.disabled)
    entity.setAttributes(details.attributes.asJava)

    details.nameBundle.foreach(bundle => entity.setName(bundle convert toLanguageBundle))
    details.descriptionBundle.foreach(bundle =>
      entity.setDescription(bundle convert toLanguageBundle)
    )
  }

  private def toLanguageBundle(
      bundle: LanguageBundle
  ): com.tle.beans.entity.LanguageBundle = {
    val languageBundle = new com.tle.beans.entity.LanguageBundle()
    languageBundle.setId(bundle.id)

    val stringsMap = bundle.strings.map { s =>
      s.locale -> toLanguageString(s, languageBundle)
    }.toMap
    languageBundle.setStrings(stringsMap.asJava)

    languageBundle
  }

  private def toLanguageString(
      string: LanguageString,
      bundle: com.tle.beans.entity.LanguageBundle
  ): com.tle.beans.entity.LanguageString = {
    val languageString = new com.tle.beans.entity.LanguageString()
    languageString.setId(string.id)
    languageString.setPriority(string.priority)
    languageString.setLocale(string.locale)
    languageString.setText(string.text)
    languageString.setBundle(bundle)

    languageString
  }
}
