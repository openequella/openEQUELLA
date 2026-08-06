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

package com.tle.web.remoting.graphql.schema.conversion

import com.tle.beans.entity.Schema
import com.tle.beans.entity.itemdef.mapping.{HTMLMapping, IMSMapping, Literal, LiteralMapping}
import com.tle.beans.entity.itemdef._
import com.tle.common.workflow.Workflow
import com.tle.core.xml.service.XmlService
import com.tle.web.remoting.graphql.schema.conversion.EntityDetailsConverter.{
  populateBaseEntity,
  toLanguageBundle
}
import com.tle.web.remoting.graphql.schema.conversion.TargetListEntryConverter.toTargetList
import com.tle.web.remoting.graphql.schema.types._

/** Converter for transforming GraphQL [[CollectionDefinition]] to the Hibernate [[ItemDefinition]]
  * entity.
  *
  * This converter handles the complete collection definition structure, producing a full-fidelity
  * reverse of the forward [[CollectionDefinition.apply]] conversion. This means a collection that
  * was read via [[CollectionDefinition.apply]] and passed back through [[toItemDefinition]] will
  * produce an [[ItemDefinition]] equivalent to the original.
  *
  * Note: XmlService is required to deserialise the opaque XML blob fields ([[Wizard]] pages and
  * fixed metadata) which are stored as plain serialised strings in the GraphQL representation.
  */
object CollectionDefinitionConverter {

  /** Converts a GraphQL collection definition to its Hibernate entity representation.
    *
    * @param from
    *   the GraphQL collection definition containing all collection configuration
    * @param xmlService
    *   used to deserialise the opaque wizard XML blob fields (pages and fixed metadata)
    * @return
    *   a new [[ItemDefinition]] entity populated with the provided values
    */
  def toItemDefinition(from: CollectionDefinition, xmlService: XmlService): ItemDefinition = {
    val to = new ItemDefinition()

    populateBaseEntity(from.details, to)

    from.schemaId.foreach(id => to.setSchema(new Schema(id)))
    from.workflowId.foreach(id => to.setWorkflow(new Workflow(id)))
    from.wizardCategory.foreach(to.setWizardcategory)
    from.reviewPeriod.foreach(to.setReviewperiod)
    from.scormPackagingTransformation.foreach(to.setScormPackagingTransformation)
    to.setDenyDirectContribution(from.denyDirectContribution)

    from.wizard.foreach(w => to.setWizard(toWizard(w, xmlService)))
    from.searchDetails.foreach(sd => to.setSearchDetails(toSearchDetails(sd)))
    from.metadataMapping.foreach(mm => to.setMetadataMapping(toMetadataMapping(mm)))

    to.setItemMetadataRules(from.itemMetadataRules.map(toItemMetadataRule).asArrayList)
    to.setDynamicMetadataRules(from.dynamicMetadataRules.map(toDynamicMetadataRule).asArrayList)

    from.itemSummaryDisplayTemplate.foreach(sdt =>
      to.setItemSummaryDisplayTemplate(toSummaryDisplayTemplate(sdt))
    )

    to
  }

  /** `XmlService` is the only supported reader of the opaque page blobs — see [[CollectionWizard]]
    * for why they are opaque and what it costs.
    */
  private def toWizard(from: CollectionWizard, xmlService: XmlService): Wizard = {
    val to = new Wizard()

    from.name.foreach(to.setName)
    from.redraftScript.foreach(to.setRedraftScript)
    from.saveScript.foreach(to.setSaveScript)
    to.setAllowNonSequentialNavigation(from.allowNonSequentialNavigation)
    to.setShowPageTitlesNextPrev(from.showPageTitlesNextPrev)
    from.additionalCssClass.foreach(to.setAdditionalCssClass)

    from.pages.foreach { xml =>
      to.setPages(xmlService.deserialiseFromXml(getClass.getClassLoader, xml))
    }
    from.fixedMetadata.foreach { xml =>
      to.setMetadata(xmlService.deserialiseFromXml(getClass.getClassLoader, xml))
    }

    to
  }

  private def toSearchDetails(from: CollectionSearchDetails): SearchDetails = {
    val to = new SearchDetails()

    from.attDisplay.foreach(to.setAttDisplay)
    to.setDisableThumbnail(from.disableThumbnail)
    to.setStandardOpen(from.standardOpen)
    to.setIntegrationOpen(from.integrationOpen)
    to.setDisplayNodes(from.displayNodes.map(toDisplayNode).asArrayList)

    to
  }

  private def toDisplayNode(from: CollectionDisplayNode): DisplayNode = {
    val to = new DisplayNode()

    from.node.foreach(to.setNode)
    from.nodeType.foreach(to.setType)
    from.mode.foreach(to.setMode)
    from.splitter.foreach(to.setSplitter)
    from.title.foreach(bundle => to.setTitle(toLanguageBundle(bundle)))
    from.truncateLength.foreach(n => to.setTruncateLength(n))

    to
  }

  private def toMetadataMapping(from: CollectionMetadataMapping): MetadataMapping = {
    val to = new MetadataMapping()

    from.imsMapping.map(toImsMapping).foreach(to.getImsMapping.add)
    from.htmlMapping.map(toHtmlMapping).foreach(to.getHtmlMapping.add)
    from.literalMapping.map(toLiteralMapping).foreach(to.getLiteralMapping.add)

    to
  }

  private def toImsMapping(from: CollectionImsMapping): IMSMapping = {
    val to = new IMSMapping()

    to.setIms(from.ims)
    to.setItemdef(from.itemdef)
    to.setType(from.mappingType)
    to.setReplace(from.replace)

    to
  }

  private def toHtmlMapping(from: CollectionHtmlMapping): HTMLMapping = {
    val to = new HTMLMapping()

    to.setHtml(from.html)
    to.setItemdef(from.itemdef)

    to
  }

  private def toLiteralMapping(from: CollectionLiteralMapping): LiteralMapping = {
    val to = new LiteralMapping()

    from.value.foreach(to.setValue)
    from.literals.map(toLiteral).foreach(to.getLiterals.add)

    to
  }

  private def toLiteral(from: CollectionLiteral): Literal = {
    val to = new Literal()

    from.value.foreach(to.setValue)
    from.script.foreach(to.setScript)

    to
  }

  private def toItemMetadataRule(from: CollectionItemMetadataRule): ItemMetadataRule = {
    val to = new ItemMetadataRule()

    from.ruleId.foreach(to.setId)
    from.name.foreach(to.setName)
    from.script.foreach(to.setScript)

    to
  }

  private def toDynamicMetadataRule(from: CollectionDynamicMetadataRule): DynamicMetadataRule = {
    val to = new DynamicMetadataRule()

    from.ruleId.foreach(to.setId)
    from.name.foreach(to.setName)
    from.path.foreach(to.setPath)
    from.ruleType.foreach(to.setType)
    to.setTargetList(from.targetList convert toTargetList)

    to
  }

  private def toSummaryDisplayTemplate(
      from: CollectionSummaryDisplayTemplate
  ): SummaryDisplayTemplate = {
    val to = new SummaryDisplayTemplate()

    to.setConfigList(from.configList.map(toSummarySectionsConfig).asArrayList)
    to.setHideOwner(from.hideOwner)
    to.setHideCollaborators(from.hideCollaborators)

    to
  }

  private def toSummarySectionsConfig(
      from: CollectionSummarySectionConfig
  ): SummarySectionsConfig = {
    val to = new SummarySectionsConfig(from.value)

    from.uuid.foreach(to.setUuid)
    from.configuration.foreach(to.setConfiguration)
    from.bundleTitle.foreach(bundle => to.setBundleTitle(toLanguageBundle(bundle)))

    to
  }
}
