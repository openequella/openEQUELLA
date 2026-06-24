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
  CollectionDefinitionEditView,
  CollectionDefinitionView,
  CollectionDisplayNodeView,
  CollectionDynamicMetadataRuleView,
  CollectionHtmlMappingView,
  CollectionImsMappingView,
  CollectionItemMetadataRuleView,
  CollectionLiteralMappingView,
  CollectionLiteralView,
  CollectionMetadataMappingView,
  CollectionSearchDetailsView,
  CollectionSummaryDisplayTemplateView,
  CollectionSummarySectionConfigView,
  CollectionWizardView
}
import io.github.openequella.graphql.client.{
  CollectionDefinitionInput,
  CollectionDisplayNodeInput,
  CollectionDynamicMetadataRuleInput,
  CollectionHtmlMappingInput,
  CollectionImsMappingInput,
  CollectionItemMetadataRuleInput,
  CollectionLiteralInput,
  CollectionLiteralMappingInput,
  CollectionMetadataMappingInput,
  CollectionSearchDetailsInput,
  CollectionSummaryDisplayTemplateInput,
  CollectionSummarySectionConfigInput,
  CollectionWizardInput,
  EditableEntityCollectionDefinitionInput
}
import io.scalaland.chimney.Transformer
import io.scalaland.chimney.dsl._

/** Provides Chimney transformers and conversion functions for collection definition types.
  *
  * This object contains:
  *   - Implicit transformers for collection-specific types
  *   - Explicit conversion functions for use in the CollectionDefinitionApi
  */
object CollectionConversions {
  import CommonConversions._

  implicit val collectionWizardViewToInput
      : Transformer[CollectionWizardView, CollectionWizardInput] =
    Transformer.derive[CollectionWizardView, CollectionWizardInput]

  implicit val collectionDisplayNodeViewToInput
      : Transformer[CollectionDisplayNodeView, CollectionDisplayNodeInput] =
    Transformer.derive[CollectionDisplayNodeView, CollectionDisplayNodeInput]

  implicit val collectionSearchDetailsViewToInput
      : Transformer[CollectionSearchDetailsView, CollectionSearchDetailsInput] =
    Transformer.derive[CollectionSearchDetailsView, CollectionSearchDetailsInput]

  implicit val collectionSummarySectionConfigViewToInput
      : Transformer[CollectionSummarySectionConfigView, CollectionSummarySectionConfigInput] =
    Transformer.derive[CollectionSummarySectionConfigView, CollectionSummarySectionConfigInput]

  implicit val collectionSummaryDisplayTemplateViewToInput
      : Transformer[CollectionSummaryDisplayTemplateView, CollectionSummaryDisplayTemplateInput] =
    Transformer
      .derive[CollectionSummaryDisplayTemplateView, CollectionSummaryDisplayTemplateInput]

  implicit val collectionLiteralViewToInput
      : Transformer[CollectionLiteralView, CollectionLiteralInput] =
    Transformer.derive[CollectionLiteralView, CollectionLiteralInput]

  implicit val collectionLiteralMappingViewToInput
      : Transformer[CollectionLiteralMappingView, CollectionLiteralMappingInput] =
    Transformer.derive[CollectionLiteralMappingView, CollectionLiteralMappingInput]

  implicit val collectionImsMappingViewToInput
      : Transformer[CollectionImsMappingView, CollectionImsMappingInput] =
    Transformer.derive[CollectionImsMappingView, CollectionImsMappingInput]

  implicit val collectionHtmlMappingViewToInput
      : Transformer[CollectionHtmlMappingView, CollectionHtmlMappingInput] =
    Transformer.derive[CollectionHtmlMappingView, CollectionHtmlMappingInput]

  implicit val collectionMetadataMappingViewToInput
      : Transformer[CollectionMetadataMappingView, CollectionMetadataMappingInput] =
    Transformer.derive[CollectionMetadataMappingView, CollectionMetadataMappingInput]

  implicit val collectionItemMetadataRuleViewToInput
      : Transformer[CollectionItemMetadataRuleView, CollectionItemMetadataRuleInput] =
    Transformer.derive[CollectionItemMetadataRuleView, CollectionItemMetadataRuleInput]

  implicit val collectionDynamicMetadataRuleViewToInput
      : Transformer[CollectionDynamicMetadataRuleView, CollectionDynamicMetadataRuleInput] =
    Transformer.derive[CollectionDynamicMetadataRuleView, CollectionDynamicMetadataRuleInput]

  implicit val collectionDefinitionViewToInput
      : Transformer[CollectionDefinitionView, CollectionDefinitionInput] =
    Transformer.derive[CollectionDefinitionView, CollectionDefinitionInput]

  implicit val collectionDefinitionEditViewToInput
      : Transformer[CollectionDefinitionEditView, EditableEntityCollectionDefinitionInput] =
    Transformer
      .define[CollectionDefinitionEditView, EditableEntityCollectionDefinitionInput]
      .withFieldRenamed(_.collection, _.entity)
      .buildTransformer

  /** Converts a CollectionDefinitionEditView to an EditableEntityCollectionDefinitionInput.
    *
    * @param view
    *   The CollectionDefinitionEditView to convert.
    * @return
    *   The corresponding EditableEntityCollectionDefinitionInput.
    */
  def toInput(view: CollectionDefinitionEditView): EditableEntityCollectionDefinitionInput =
    view.transformInto[EditableEntityCollectionDefinitionInput]
}
