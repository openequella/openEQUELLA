/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.validation

import java.time.Instant
import java.util.Locale

import cats.data.{Validated, ValidatedNec}
import com.tle.core.db.types.LocaleStrings
import cats.syntax.validated._
import com.tle.core.db.tables.OEQEntity

case class EntityValidation(field: String, reason: String)

trait OEQEntityEdits {
  def name: String
  def nameStrings: Option[Map[String, String]]
  def description: Option[String]
  def descriptionStrings: Option[Map[String, String]]
}

object EntityValidation {

  final val NameField = "name"
  final val FailBlank = "blank"

  def nonBlankStrings(
      field: String,
      string: String,
      strings: Option[Map[String, String]],
      locale: Locale
  ): ValidatedNec[EntityValidation, (String, LocaleStrings)] = {
    LocaleStrings
      .fromStrings(string, strings, locale)
      .map(_.validNec)
      .getOrElse(EntityValidation(field, FailBlank).invalidNec)
  }

  def nonBlank(field: String,
               string: String,
               locale: Locale): ValidatedNec[EntityValidation, (String, LocaleStrings)] =
    nonBlankStrings(field, string, None, locale)

  def nonBlankString(field: String, string: String): ValidatedNec[EntityValidation, String] =
    Validated.condNec(string.nonEmpty, string, EntityValidation(field, FailBlank))

  def standardValidation(
      edits: OEQEntityEdits,
      oeq: OEQEntity,
      locale: Locale
  ): ValidatedNec[EntityValidation, OEQEntity] = {
    EntityValidation
      .nonBlankStrings(NameField, edits.name, edits.nameStrings, locale)
      .map { n =>
        val desc = LocaleStrings.fromStrings(
          edits.description.getOrElse(""),
          edits.descriptionStrings,
          locale
        )
        oeq.copy(
          name = n._1,
          name_strings = n._2,
          description = desc.map(_._1),
          modified = Instant.now,
          description_strings = desc.map(_._2).getOrElse(LocaleStrings.empty)
        )
      }
  }
}
