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

package com.tle.core.validation

import cats.data.{NonEmptyChain, Validated, ValidatedNec}
import cats.implicits._
import com.tle.core.i18n.LocaleStrings
import java.util.Locale

/** Data structure for Entity standard fields.
  *
  * @param name
  *   Name of the Entity.
  * @param nameStrings
  *   Name of the Entity including current locale.
  * @param description
  *   Description of the Entity.
  * @param descriptionStrings
  *   Description of the Entity including current locale.
  */
case class EntityStdEdits(
    name: String,
    nameStrings: Option[Map[String, String]] = None,
    description: Option[String] = None,
    descriptionStrings: Option[Map[String, String]] = None
)

/** Data structure representing a failed Entity validation.
  *
  * @param field
  *   The field being validated.
  * @param reason
  *   Why the validation fails.
  */
case class EntityValidation(field: String, reason: String) {
  override def toString: String = s"$field: $reason"
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

  def nonBlank(
      field: String,
      string: String,
      locale: Locale
  ): ValidatedNec[EntityValidation, (String, LocaleStrings)] =
    nonBlankStrings(field, string, None, locale)

  def nonBlankString(field: String, string: String): ValidatedNec[EntityValidation, String] =
    Validated.condNec(string.nonEmpty, string, EntityValidation(field, FailBlank))

  def standardValidation(
      edits: EntityStdEdits,
      locale: Locale
  ): ValidatedNec[EntityValidation, EntityStdEdits] = {
    EntityValidation
      .nonBlankStrings(NameField, edits.name, edits.nameStrings, locale)
      .map(validated => {
        val (name, nameStrings) = validated
        val (desc, descStrings) = LocaleStrings
          .fromStrings(
            edits.description.getOrElse(""),
            edits.descriptionStrings,
            locale
          )
          .separate

        // Return a copy of the provided edits with validated values.
        edits.copy(name, Option(nameStrings.strings), desc, descStrings.map(_.strings))
      })
  }

  /** Given a list of EntityValidation, which typically is wrapped by a NonEmptyChain, convert each
    * EntityValidation to a more readable error message and put all messages in one list.
    *
    * @param validations
    *   A list of EntityValidation captured during the process of `Entity`
    * @return
    *   A list of error messages transformed from the list of EntityValidation.
    */
  def collectErrors(validations: NonEmptyChain[EntityValidation]): List[String] =
    validations.toList.map(_.toString)
}
