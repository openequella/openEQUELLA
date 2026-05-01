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
import com.tle.beans.entity.itemdef.{SummaryDisplayTemplate, SummarySectionsConfig}

import scala.jdk.CollectionConverters._

/** GraphQL representation of `com.tle.beans.entity.itemdef.SummaryDisplayTemplate`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.SummaryDisplayTemplate]]
  */
@GQLDescription("Template controlling how item summary pages are displayed.")
final case class CollectionSummaryDisplayTemplate(
    @GQLDescription("The ordered list of section configurations for this template")
    configList: List[CollectionSummarySectionConfig],
    @GQLDescription("Whether the item owner is hidden from the summary page")
    hideOwner: Boolean,
    @GQLDescription("Whether collaborators are hidden from the summary page")
    hideCollaborators: Boolean
)

object CollectionSummaryDisplayTemplate {
  def apply(template: SummaryDisplayTemplate): CollectionSummaryDisplayTemplate =
    CollectionSummaryDisplayTemplate(
      configList = Option(template.getConfigList)
        .map(_.asScala.toList.map(CollectionSummarySectionConfig(_)))
        .getOrElse(List.empty),
      hideOwner = template.isHideOwner,
      hideCollaborators = template.isHideCollaborators
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.SummarySectionsConfig`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.SummarySectionsConfig]]
  */
@GQLDescription("Configuration for a single section in the item summary display template.")
final case class CollectionSummarySectionConfig(
    @GQLDescription("Unique identifier for this section configuration")
    uuid: Option[String],
    @GQLDescription("The section type identifier (plugin extension ID)")
    value: String,
    @GQLDescription("Plugin-specific XML configuration for this section")
    configuration: Option[String],
    @GQLDescription("Display title override for this section")
    bundleTitle: Option[LanguageBundle]
)

object CollectionSummarySectionConfig {
  def apply(config: SummarySectionsConfig): CollectionSummarySectionConfig =
    CollectionSummarySectionConfig(
      uuid = Option(config.getUuid),
      value = config.getValue,
      configuration = Option(config.getConfiguration),
      bundleTitle = Option(config.getBundleTitle).map(LanguageBundle(_))
    )
}
