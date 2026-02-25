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

package io.github.openequella.graphql.api.views.conversions

import io.github.openequella.graphql.api.views.{
  EntityDetailsView,
  LanguageBundleView,
  LanguageStringView,
  TargetListEntryView
}
import io.github.openequella.graphql.client.{
  EntityDetailsInput,
  KVStringStringInput,
  LanguageBundleInput,
  LanguageStringInput,
  TargetListEntryInput
}
import io.scalaland.chimney.Transformer

/** Provides implicit Chimney transformers for common entity types shared across multiple APIs.
  *
  * These transformers handle the conversion of shared view types (returned by GraphQL queries) to
  * their corresponding input types (required by GraphQL mutations).
  */
object CommonConversions {

  /** Transformer for LanguageStringView to LanguageStringInput. */
  implicit val languageStringViewToInput: Transformer[LanguageStringView, LanguageStringInput] =
    Transformer.derive[LanguageStringView, LanguageStringInput]

  /** Transformer for LanguageBundleView to LanguageBundleInput. */
  implicit val languageBundleViewToInput: Transformer[LanguageBundleView, LanguageBundleInput] =
    Transformer.derive[LanguageBundleView, LanguageBundleInput]

  /** Transformer for EntityDetailsView to EntityDetailsInput.
    *
    * Note: The `attributes` field requires custom handling as EntityDetailsView uses
    * `Map[String, String]` while EntityDetailsInput uses `List[KVStringStringInput]`.
    */
  implicit val entityDetailsViewToInput: Transformer[EntityDetailsView, EntityDetailsInput] =
    Transformer
      .define[EntityDetailsView, EntityDetailsInput]
      .withFieldComputed(
        _.attributes,
        view => view.attributes.map { case (k, v) => KVStringStringInput(k, v) }.toList
      )
      .buildTransformer

  /** Transformer for TargetListEntryView to TargetListEntryInput. */
  implicit val targetListEntryViewToInput: Transformer[TargetListEntryView, TargetListEntryInput] =
    Transformer.derive[TargetListEntryView, TargetListEntryInput]
}
