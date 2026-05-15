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

package com.tle.admin.graphql.conversion

import com.tle.admin.graphql.conversion.CollectionDynamicMetadataRuleViewConverter.toDynamicMetadataRule
import com.tle.admin.graphql.conversion.CollectionItemMetadataRuleViewConverter.toItemMetadataRule
import com.tle.admin.graphql.conversion.CollectionMetadataMappingViewConverter.toMetadataMapping
import com.tle.admin.graphql.conversion.CollectionSearchDetailsViewConverter.toSearchDetails
import com.tle.admin.graphql.conversion.CollectionSummaryDisplayTemplateViewConverter.toSummaryDisplayTemplate
import com.tle.admin.graphql.conversion.CollectionWizardViewConverter.toWizard
import com.tle.admin.graphql.conversion.EntityDetailsViewConverter.applyToBaseEntity
import com.tle.beans.entity.Schema
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.common.workflow.Workflow
import io.github.openequella.graphql.api.views.CollectionDefinitionView

import scala.util.chaining.scalaUtilChainingOps

object CollectionDefinitionViewConverter {
  private val NO_REVIEW_PERIOD = Integer.MIN_VALUE

  def toItemDefinition(view: CollectionDefinitionView): ItemDefinition =
    new ItemDefinition(view.details.id).tap { itemDef =>
      // Set the base entity details
      applyToBaseEntity(itemDef, view.details)

      // Setup relationships to other entities
      // WARNING: These are being set with only the ID - and hasn't received all the details of the
      // related entity. This is possibly different to the old HttpInvoker which was more eager.
      // Will have to rely on testing to establish if this is okay. If not we'll have to make calls
      // to fetch the details of the related entities.
      view.schemaId.foreach(id => itemDef.setSchema(new Schema(id)))
      view.workflowId.foreach(id => itemDef.setWorkflow(new Workflow(id)))

      // Set the simple values
      itemDef.setWizardcategory(view.wizardCategory.orNull)
      itemDef.setReviewperiod(view.reviewPeriod.getOrElse(NO_REVIEW_PERIOD))
      itemDef.setScormPackagingTransformation(view.scormPackagingTransformation.orNull)
      itemDef.setDenyDirectContribution(view.denyDirectContribution)

      // Set values which need conversion
      view.wizard.map(toWizard).foreach(itemDef.setWizard)
      view.searchDetails.map(toSearchDetails).foreach(itemDef.setSearchDetails)
      view.metadataMapping.map(toMetadataMapping).foreach(itemDef.setMetadataMapping)
      view.itemSummaryDisplayTemplate
        .map(toSummaryDisplayTemplate)
        .foreach(itemDef.setItemSummaryDisplayTemplate)

      // Set the collections - which also need conversion to a suitable Java type collection.
      itemDef.setItemMetadataRules(view.itemMetadataRules.map(toItemMetadataRule).asArrayList)
      itemDef.setDynamicMetadataRules(
        view.dynamicMetadataRules.map(toDynamicMetadataRule).asArrayList
      )
    }
}
