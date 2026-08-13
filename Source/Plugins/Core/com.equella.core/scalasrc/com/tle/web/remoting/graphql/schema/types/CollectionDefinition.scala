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
import com.tle.core.xml.service.XmlService

/** GraphQL representation of `com.tle.beans.entity.itemdef.ItemDefinition`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.ItemDefinition]]
  */
@GQLDescription(
  "A collection definition, defining the structure and configuration for a collection of items in openEQUELLA."
)
final case class CollectionDefinition(
    @GQLDescription("Common details of the collection definition, including UUID and owner.")
    details: EntityDetails,
    @GQLDescription("ID of the metadata schema associated with this collection.")
    schemaId: Option[Long],
    @GQLDescription("Category identifier for the wizard used by this collection.")
    wizardCategory: Option[String],
    @GQLDescription("ID of the workflow associated with this collection.")
    workflowId: Option[Long],
    @GQLDescription(
      "The review period in days. None if no review period is configured for this collection."
    )
    reviewPeriod: Option[Int],
    @GQLDescription("Name of the XSLT used for SCORM packaging.")
    scormPackagingTransformation: Option[String],
    @GQLDescription("Whether direct contribution by users is denied for this collection.")
    denyDirectContribution: Boolean,
    @GQLDescription("Contribution wizard configuration for this collection.")
    wizard: Option[CollectionWizard],
    @GQLDescription("Search results display configuration for this collection.")
    searchDetails: Option[CollectionSearchDetails],
    @GQLDescription("Metadata import mapping configuration for this collection.")
    metadataMapping: Option[CollectionMetadataMapping],
    @GQLDescription(
      "Rules controlling access to items in this collection based on their metadata."
    )
    itemMetadataRules: List[CollectionItemMetadataRule],
    @GQLDescription("Rules that apply dynamic ACL entries to items based on their metadata.")
    dynamicMetadataRules: List[CollectionDynamicMetadataRule],
    @GQLDescription("Summary page display template for items in this collection.")
    itemSummaryDisplayTemplate: Option[CollectionSummaryDisplayTemplate]
)

object CollectionDefinition {

  /** Converts an `ItemDefinition` entity to a `CollectionDefinition` for use in GraphQL.
    *
    * @param entity
    *   The ItemDefinition entity to convert.
    * @param xmlService
    *   Used to serialise plugin-extensible XStream blob fields (wizard pages, fixed metadata).
    * @return
    *   A `CollectionDefinition` representation of the entity.
    */
  def apply(
      entity: com.tle.beans.entity.itemdef.ItemDefinition,
      xmlService: XmlService
  ): CollectionDefinition =
    CollectionDefinition(
      details = EntityDetails(entity),
      schemaId = Option(entity.getSchema).map(_.getId),
      wizardCategory = Option(entity.getWizardcategory),
      workflowId = Option(entity.getWorkflow).map(_.getId),
      reviewPeriod = Option.when(entity.hasReviewPeriod)(entity.getReviewperiod),
      scormPackagingTransformation = Option(entity.getScormPackagingTransformation),
      denyDirectContribution = entity.isDenyDirectContribution,
      wizard = Option(entity.getWizard).map(CollectionWizard(_, xmlService)),
      searchDetails = Option(entity.getSearchDetails).map(CollectionSearchDetails(_)),
      metadataMapping = Option(entity.getMetadataMapping).map(CollectionMetadataMapping(_)),
      itemMetadataRules =
        convertJavaList(entity.getItemMetadataRules)(CollectionItemMetadataRule(_)),
      dynamicMetadataRules =
        convertJavaList(entity.getDynamicMetadataRules)(CollectionDynamicMetadataRule(_)),
      itemSummaryDisplayTemplate =
        Option(entity.getItemSummaryDisplayTemplate).map(CollectionSummaryDisplayTemplate(_))
    )
}
