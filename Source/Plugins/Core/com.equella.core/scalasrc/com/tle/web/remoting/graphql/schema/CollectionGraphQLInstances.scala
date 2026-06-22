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

package com.tle.web.remoting.graphql.schema

import caliban.schema.ArgBuilder
import caliban.schema.Schema
import com.tle.web.remoting.graphql.schema.types._

/** Explicit semi-auto caliban typeclass instances for the [[CollectionDefinition]] input/output
  * type tree.
  *
  * '''Why this object exists'''
  *
  * Every other schema in this module uses caliban's full-auto derivation (`ArgBuilder.auto._` /
  * `Schema.auto._`). With auto derivation, Magnolia inlines the entire typeclass instance at the
  * use site. Adding `stopEdit` makes [[CollectionDefinition]] an '''input''' type for the first
  * time, and its nested tree (wizard, search details, metadata mappings, rules, summary template
  * ...) is deep enough that the full inline expansion exceeds the JVM's 64 KB per-method bytecode
  * limit, causing:
  * {{{
  *   [error] Method too large: CollectionSchema.getApi ()Lcaliban/GraphQL;
  * }}}
  *
  * '''Fix'''
  *
  * By providing named `implicit lazy val` instances here — using semi-auto `ArgBuilder.gen[T]` /
  * `Schema.gen` — each instance compiles to its own JVM field/method initializer. When caliban
  * derives a parent type it references those already-computed instances (a field load) instead of
  * re-inlining the subtree, keeping `getApi`'s bytecode within the JVM limit.
  *
  * Instances are declared bottom-up (leaf types first) so that when a parent type's `gen` macro
  * runs it picks up its children's explicit instances from this same object.
  *
  * Importing `CollectionGraphQLInstances._` into [[CollectionSchema]] gives these instances
  * priority over the `auto._` wildcard imports for the covered types. The `auto._` imports remain
  * in place for all other types (simple args, wrapper types, etc.).
  *
  * Note: No wildcard `auto._` import is needed here. Primitive instances (`Long`, `String`,
  * `Boolean`, etc.) are always found via the companion objects of `ArgBuilder` / `Schema`.
  */
object CollectionGraphQLInstances {

  // Leaf / shared types

  implicit lazy val languageStringArgBuilder: ArgBuilder[LanguageString] = ArgBuilder.gen
  implicit lazy val languageStringSchema: Schema[Any, LanguageString]    = Schema.gen

  implicit lazy val languageBundleArgBuilder: ArgBuilder[LanguageBundle] = ArgBuilder.gen
  implicit lazy val languageBundleSchema: Schema[Any, LanguageBundle]    = Schema.gen

  implicit lazy val entityDetailsArgBuilder: ArgBuilder[EntityDetails] = ArgBuilder.gen
  implicit lazy val entityDetailsSchema: Schema[Any, EntityDetails]    = Schema.gen

  implicit lazy val targetListEntryArgBuilder: ArgBuilder[TargetListEntry] = ArgBuilder.gen
  implicit lazy val targetListEntrySchema: Schema[Any, TargetListEntry]    = Schema.gen

  implicit lazy val otherTargetListTypeArgBuilder: ArgBuilder[OtherTargetListType] = ArgBuilder.gen
  implicit lazy val otherTargetListTypeSchema: Schema[Any, OtherTargetListType]    = Schema.gen

  implicit lazy val otherTargetListArgBuilder: ArgBuilder[OtherTargetList] = ArgBuilder.gen
  implicit lazy val otherTargetListSchema: Schema[Any, OtherTargetList]    = Schema.gen

  // Collection sub-types

  implicit lazy val collectionWizardArgBuilder: ArgBuilder[CollectionWizard] = ArgBuilder.gen
  implicit lazy val collectionWizardSchema: Schema[Any, CollectionWizard]    = Schema.gen

  implicit lazy val collectionDisplayNodeArgBuilder: ArgBuilder[CollectionDisplayNode] =
    ArgBuilder.gen
  implicit lazy val collectionDisplayNodeSchema: Schema[Any, CollectionDisplayNode] = Schema.gen

  implicit lazy val collectionSearchDetailsArgBuilder: ArgBuilder[CollectionSearchDetails] =
    ArgBuilder.gen
  implicit lazy val collectionSearchDetailsSchema: Schema[Any, CollectionSearchDetails] = Schema.gen

  implicit lazy val collectionImsMappingArgBuilder: ArgBuilder[CollectionImsMapping] =
    ArgBuilder.gen
  implicit lazy val collectionImsMappingSchema: Schema[Any, CollectionImsMapping] = Schema.gen

  implicit lazy val collectionHtmlMappingArgBuilder: ArgBuilder[CollectionHtmlMapping] =
    ArgBuilder.gen
  implicit lazy val collectionHtmlMappingSchema: Schema[Any, CollectionHtmlMapping] = Schema.gen

  implicit lazy val collectionLiteralArgBuilder: ArgBuilder[CollectionLiteral] = ArgBuilder.gen
  implicit lazy val collectionLiteralSchema: Schema[Any, CollectionLiteral]    = Schema.gen

  implicit lazy val collectionLiteralMappingArgBuilder: ArgBuilder[CollectionLiteralMapping] =
    ArgBuilder.gen
  implicit lazy val collectionLiteralMappingSchema: Schema[Any, CollectionLiteralMapping] =
    Schema.gen

  implicit lazy val collectionMetadataMappingArgBuilder: ArgBuilder[CollectionMetadataMapping] =
    ArgBuilder.gen
  implicit lazy val collectionMetadataMappingSchema: Schema[Any, CollectionMetadataMapping] =
    Schema.gen

  implicit lazy val collectionItemMetadataRuleArgBuilder: ArgBuilder[CollectionItemMetadataRule] =
    ArgBuilder.gen
  implicit lazy val collectionItemMetadataRuleSchema: Schema[Any, CollectionItemMetadataRule] =
    Schema.gen

  implicit lazy val collectionDynamicMetadataRuleArgBuilder
      : ArgBuilder[CollectionDynamicMetadataRule] = ArgBuilder.gen
  implicit lazy val collectionDynamicMetadataRuleSchema
      : Schema[Any, CollectionDynamicMetadataRule] = Schema.gen

  implicit lazy val collectionSummarySectionConfigArgBuilder
      : ArgBuilder[CollectionSummarySectionConfig] = ArgBuilder.gen
  implicit lazy val collectionSummarySectionConfigSchema
      : Schema[Any, CollectionSummarySectionConfig] = Schema.gen

  implicit lazy val collectionSummaryDisplayTemplateArgBuilder
      : ArgBuilder[CollectionSummaryDisplayTemplate] = ArgBuilder.gen
  implicit lazy val collectionSummaryDisplayTemplateSchema
      : Schema[Any, CollectionSummaryDisplayTemplate] = Schema.gen

  // Top-level collection types

  implicit lazy val collectionDefinitionArgBuilder: ArgBuilder[CollectionDefinition] =
    ArgBuilder.gen
  implicit lazy val collectionDefinitionSchema: Schema[Any, CollectionDefinition] = Schema.gen

  implicit lazy val editableEntityCollectionArgBuilder
      : ArgBuilder[EditableEntity[CollectionDefinition]] = ArgBuilder.gen
  implicit lazy val editableEntityCollectionSchema
      : Schema[Any, EditableEntity[CollectionDefinition]] = Schema.gen
}
