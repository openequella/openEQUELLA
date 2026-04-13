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
import io.github.openequella.graphql.client.Language

/** View model for a configured language entry.
  *
  * @param id
  *   Unique identifier for the language entry.
  * @param language
  *   Language code (e.g., "en" for English).
  * @param country
  *   Country code (e.g., "AU" for Australia).
  * @param variant
  *   Locale variant.
  */
final case class LanguageView(
    id: Long,
    language: String,
    country: String,
    variant: String
)

object LanguageView {
  val selector: SelectionBuilder[Language, LanguageView] = (
    Language.id ~ Language.language ~ Language.country ~ Language.variant
  ).mapN(LanguageView.apply _)
}
