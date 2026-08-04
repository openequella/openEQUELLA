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
  * ==Why `pages` and `fixedMetadata` are opaque XML==
  *
  * They carry the legacy `Wizard` object graph serialised by XStream — the same bytes the database
  * holds in the `xstream_immutable` blob columns — rather than a modelled GraphQL structure.
  *
  * That was a deliberate scoping decision, not an oversight. This API exists to retire Spring's
  * HttpInvoker, which modern Spring has dropped and which is therefore pinning oEQ to old versions
  * of Spring and other major dependencies. Modelling the wizard properly means a GraphQL type per
  * wizard control (there are around thirty), plus their nested `WizardControlItem`, `TargetNode`
  * and `DRMPage` structures, and matching typed editors in the Admin Console. Passing the existing
  * object graph through untouched kept the migration to a size that could actually be finished.
  *
  * ==The cost of that decision==
  *
  * An opaque blob turns the wire format into a shared-configuration contract: both ends must use
  * [[com.tle.core.xstream.ExtXStream]], because oEQ's format predates the XStream defaults and a
  * stock instance can neither read nor write it. When the Admin Console used a stock instance,
  * every collection holding a single-locale language bundle — anything with a Raw HTML control set
  * to a predefined element, for instance — failed to open. Nothing in the type system prevents that
  * recurring; only the convention that these strings are produced and consumed by `ExtXStream`.
  *
  * ==What finishing the job looks like==
  *
  * Wizard controls modelled as a GraphQL union or interface, typed views replacing the blob in the
  * Admin Console editors, and these two fields dropped from the schema. The database can keep its
  * XML representation independently — this is about the API, not the persistence format.
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
