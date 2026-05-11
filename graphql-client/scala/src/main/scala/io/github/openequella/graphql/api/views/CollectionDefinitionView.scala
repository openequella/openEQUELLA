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
import io.github.openequella.graphql.client.CollectionDefinition

/** View model for a collection definition, including configuration details and content rules.
  *
  * @param details
  *   Entity details for the collection.
  * @param schemaId
  *   ID of the metadata schema associated with this collection.
  * @param wizardCategory
  *   Category identifier for the wizard used by this collection.
  * @param workflowId
  *   ID of the workflow associated with this collection.
  * @param reviewPeriod
  *   The review period in days. None if no review period is configured for this collection.
  * @param scormPackagingTransformation
  *   Name of the XSLT used for SCORM packaging.
  * @param denyDirectContribution
  *   Whether direct contribution by users is denied for this collection.
  * @param wizard
  *   Contribution wizard configuration for this collection.
  * @param searchDetails
  *   Search results display configuration for this collection.
  * @param metadataMapping
  *   Metadata mapping configuration for external standards (IMS/LOM, HTML, literals).
  * @param itemMetadataRules
  *   Rules controlling access to item metadata based on collection rules.
  * @param dynamicMetadataRules
  *   Dynamic ACL rules based on metadata values.
  * @param itemSummaryDisplayTemplate
  *   Item summary page display configuration for this collection.
  */
final case class CollectionDefinitionView(
    details: EntityDetailsView,
    schemaId: Option[Long],
    wizardCategory: Option[String],
    workflowId: Option[Long],
    reviewPeriod: Option[Int],
    scormPackagingTransformation: Option[String],
    denyDirectContribution: Boolean,
    wizard: Option[CollectionWizardView],
    searchDetails: Option[CollectionSearchDetailsView],
    metadataMapping: Option[CollectionMetadataMappingView],
    itemMetadataRules: List[CollectionItemMetadataRuleView],
    dynamicMetadataRules: List[CollectionDynamicMetadataRuleView],
    itemSummaryDisplayTemplate: Option[CollectionSummaryDisplayTemplateView]
)

object CollectionDefinitionView {
  val selector: SelectionBuilder[CollectionDefinition, CollectionDefinitionView] =
    (
      CollectionDefinition.details(EntityDetailsView.selector) ~
        CollectionDefinition.schemaId ~
        CollectionDefinition.wizardCategory ~
        CollectionDefinition.workflowId ~
        CollectionDefinition.reviewPeriod ~
        CollectionDefinition.scormPackagingTransformation ~
        CollectionDefinition.denyDirectContribution ~
        CollectionDefinition.wizard(CollectionWizardView.selector) ~
        CollectionDefinition.searchDetails(CollectionSearchDetailsView.selector) ~
        CollectionDefinition.metadataMapping(CollectionMetadataMappingView.selector) ~
        CollectionDefinition.itemMetadataRules(CollectionItemMetadataRuleView.selector) ~
        CollectionDefinition.dynamicMetadataRules(CollectionDynamicMetadataRuleView.selector) ~
        CollectionDefinition.itemSummaryDisplayTemplate(
          CollectionSummaryDisplayTemplateView.selector
        )
    ).mapN(CollectionDefinitionView.apply _)
}
