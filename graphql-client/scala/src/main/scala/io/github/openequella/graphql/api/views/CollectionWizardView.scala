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
import io.github.openequella.graphql.client.CollectionWizard

/** View model for collection contribution wizard configuration.
  *
  * @param name
  *   Display name of the wizard.
  * @param redraftScript
  *   Script to run when an item is redrafted.
  * @param saveScript
  *   Script to run when an item is saved.
  * @param allowNonSequentialNavigation
  *   Whether contributors can navigate wizard pages non-sequentially.
  * @param showPageTitlesNextPrev
  *   Whether to show page titles in the next/previous navigation.
  * @param additionalCssClass
  *   Additional CSS class applied to the wizard container.
  * @param pages
  *   Internal use only. Opaque serialised XML of the wizard pages.
  * @param fixedMetadata
  *   Internal use only. Opaque serialised XML of the fixed metadata applied by the wizard.
  */
final case class CollectionWizardView(
    name: Option[String],
    redraftScript: Option[String],
    saveScript: Option[String],
    allowNonSequentialNavigation: Boolean,
    showPageTitlesNextPrev: Boolean,
    additionalCssClass: Option[String],
    pages: Option[String],
    fixedMetadata: Option[String]
)

object CollectionWizardView {
  val selector: SelectionBuilder[CollectionWizard, CollectionWizardView] =
    (
      CollectionWizard.name ~
        CollectionWizard.redraftScript ~
        CollectionWizard.saveScript ~
        CollectionWizard.allowNonSequentialNavigation ~
        CollectionWizard.showPageTitlesNextPrev ~
        CollectionWizard.additionalCssClass ~
        CollectionWizard.pages ~
        CollectionWizard.fixedMetadata
    ).mapN(CollectionWizardView.apply _)
}
