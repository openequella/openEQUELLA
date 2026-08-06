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
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

@GQLDescription("A single translation of a piece of text for a specific locale.")
final case class LanguageString(
    @GQLDescription("Unique identifier for this language string.")
    id: Long,
    @GQLDescription(
      "Priority of this language string, with lower numbers indicating higher priority."
    )
    priority: Int,
    @GQLDescription("Locale code for this language string, e.g., 'en' for English.")
    locale: String,
    @GQLDescription("The translated text for this locale.")
    text: String
)

@GQLDescription(
  "A bundle of language strings, each representing a translation for a specific locale."
)
final case class LanguageBundle(
    @GQLDescription("Unique identifier for this language bundle.")
    id: Long,
    @GQLDescription("List of language strings in this bundle.")
    strings: List[LanguageString]
)
object LanguageBundle {
  private val LOGGER = LoggerFactory.getLogger(classOf[LanguageBundle])

  def apply(bundle: com.tle.beans.entity.LanguageBundle): LanguageBundle =
    LanguageBundle(
      id = bundle.getId,
      strings = Option(bundle.getStrings)
        .map(_.asScala.values.toList)
        .getOrElse(List.empty)
        .flatMap(toLanguageString)
    )

  /** Converts a Java [[com.tle.beans.entity.LanguageString]] to its GraphQL representation,
    * returning [[None]] if the entry is null or has null required fields (`locale` or `text`).
    *
    * Null entries can arise from legacy data. Since both `locale` and `text` are non-nullable in
    * the GraphQL schema (`String!`), passing null values through would cause caliban's renderer to
    * crash. Entries with null required fields are logged and dropped rather than propagated.
    */
  private def toLanguageString(
      langString: com.tle.beans.entity.LanguageString
  ): Option[LanguageString] = {
    val result = Option(langString)
      .filter(_.getLocale != null)
      .filter(_.getText != null)
      .map { ls =>
        LanguageString(
          id = ls.getId,
          priority = ls.getPriority,
          locale = ls.getLocale,
          text = ls.getText
        )
      }

    if (result.isEmpty) {
      val context = Option(langString)
        .map(ls =>
          s"id=${ls.getId}, locale=${Option(ls.getLocale).getOrElse("null")}, text=${Option(ls.getText)
              .fold("null")(t => s"'${t.take(20)}...'")}"
        )
        .getOrElse("null entry")
      LOGGER.debug(s"Dropping invalid LanguageString ($context) from bundle")
    }
    result
  }
}

/** Unlike [[LanguageBundle]], it represents a resolved display text for a language bundle, where
  * the most appropriate translation has been selected based on the system states.
  */
@GQLDescription("Resolved display text for a language bundle.")
final case class LanguageBundleName(
    @GQLDescription("The language bundle ID.")
    id: Long,
    @GQLDescription("The closest resolved display text for the language bundle.")
    string: String
)
