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

/** GraphQL representation of [[com.tle.beans.Language]].
  */
@GQLDescription(
  "A language configuration entry, representing a specific locale."
)
final case class Language(
    @GQLDescription("Unique identifier for this language entry.")
    id: Long,
    @GQLDescription("Language code, e.g., 'en' for English.")
    language: String,
    @GQLDescription("Country code, e.g., 'AU' for Australia.")
    country: String,
    @GQLDescription("Locale variant.")
    variant: String
)

object Language {
  def apply(language: com.tle.beans.Language): Language =
    Language(
      id = language.getId,
      language = language.getLanguage,
      country = language.getCountry,
      variant = language.getVariant
    )
}
