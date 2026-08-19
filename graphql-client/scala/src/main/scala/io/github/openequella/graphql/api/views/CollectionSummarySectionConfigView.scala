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
import io.github.openequella.graphql.client.CollectionSummarySectionConfig

/** View model for a section configuration in the item summary display template.
  *
  * @param uuid
  *   Unique identifier for this section configuration.
  * @param value
  *   The section type identifier (plugin extension ID).
  * @param configuration
  *   Plugin-specific XML configuration for this section.
  * @param bundleTitle
  *   Display title override for this section.
  */
final case class CollectionSummarySectionConfigView(
    uuid: Option[String],
    value: String,
    configuration: Option[String],
    bundleTitle: Option[LanguageBundleView]
)

object CollectionSummarySectionConfigView {
  val selector
      : SelectionBuilder[CollectionSummarySectionConfig, CollectionSummarySectionConfigView] =
    (
      CollectionSummarySectionConfig.uuid ~
        CollectionSummarySectionConfig.value ~
        CollectionSummarySectionConfig.configuration ~
        CollectionSummarySectionConfig.bundleTitle(LanguageBundleView.selector)
    ).mapN(CollectionSummarySectionConfigView.apply _)
}
