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
import io.github.openequella.graphql.client.{LanguageBundle, LanguageString}

/** View model for a language string, representing a localized text entry.
  *
  * @param id
  *   Unique identifier for the language string.
  * @param priority
  *   Priority of the language string within its bundle.
  * @param locale
  *   Locale code (e.g., "en", "fr") for the language string.
  * @param text
  *   The actual localized text.
  */
final case class LanguageStringView(id: Long, priority: Int, locale: String, text: String)
object LanguageStringView {
  val selector: SelectionBuilder[LanguageString, LanguageStringView] =
    (LanguageString.id ~ LanguageString.priority ~ LanguageString.locale ~ LanguageString.text)
      .mapN(LanguageStringView.apply _)
}

/** View model for a language bundle, which is a collection of language strings.
  *
  * @param id
  *   Unique identifier for the language bundle.
  * @param strings
  *   List of language strings contained in the bundle.
  */
final case class LanguageBundleView(id: Long, strings: List[LanguageStringView])
object LanguageBundleView {
  val selector: SelectionBuilder[LanguageBundle, LanguageBundleView] =
    (
      LanguageBundle.id ~ LanguageBundle.strings(LanguageStringView.selector)
    ).mapN(LanguageBundleView.apply _)
}
