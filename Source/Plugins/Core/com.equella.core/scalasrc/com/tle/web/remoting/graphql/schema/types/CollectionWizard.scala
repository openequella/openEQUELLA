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
import com.tle.beans.entity.itemdef.Wizard
import com.tle.core.xml.service.XmlService

/** GraphQL representation of `com.tle.beans.entity.itemdef.Wizard`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.Wizard]]
  */
@GQLDescription("Configuration for the contribution wizard for a collection.")
final case class CollectionWizard(
    @GQLDescription("Display name of the wizard")
    name: Option[String],
    @GQLDescription("Script to run when an item is redrafted")
    redraftScript: Option[String],
    @GQLDescription("Script to run when an item is saved")
    saveScript: Option[String],
    @GQLDescription("Whether contributors can navigate wizard pages non-sequentially")
    allowNonSequentialNavigation: Boolean,
    @GQLDescription("Whether to show page titles in the next/previous navigation")
    showPageTitlesNextPrev: Boolean,
    @GQLDescription("Additional CSS class applied to the wizard container")
    additionalCssClass: Option[String],
    @GQLDescription(
      "Internal use only. Opaque serialised XML of the wizard pages. " +
        "Do not parse this field directly — use dedicated APIs for wizard page manipulation."
    )
    pages: Option[String],
    @GQLDescription(
      "Internal use only. Opaque serialised XML of the fixed metadata applied by the wizard."
    )
    fixedMetadata: Option[String]
)

object CollectionWizard {
  def apply(wizard: Wizard, xmlService: XmlService): CollectionWizard =
    CollectionWizard(
      name = Option(wizard.getName),
      redraftScript = Option(wizard.getRedraftScript),
      saveScript = Option(wizard.getSaveScript),
      allowNonSequentialNavigation = wizard.isAllowNonSequentialNavigation,
      showPageTitlesNextPrev = wizard.isShowPageTitlesNextPrev,
      additionalCssClass = Option(wizard.getAdditionalCssClass),
      pages = Option(wizard.getPages).map(xmlService.serialiseToXml),
      fixedMetadata = Option(wizard.getMetadata).map(xmlService.serialiseToXml)
    )
}
