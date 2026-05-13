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
import io.github.openequella.graphql.client.CollectionSearchDetails

/** View model for search results display configuration in a collection.
  *
  * @param attDisplay
  *   Controls how attachments are displayed in search results.
  * @param disableThumbnail
  *   Whether to disable the thumbnail display in search results.
  * @param standardOpen
  *   Whether search results open in the standard view by default.
  * @param integrationOpen
  *   Whether search results open in integration contexts by default.
  * @param displayNodes
  *   The list of metadata nodes to display for each item in search results.
  */
final case class CollectionSearchDetailsView(
    attDisplay: Option[String],
    disableThumbnail: Boolean,
    standardOpen: Boolean,
    integrationOpen: Boolean,
    displayNodes: List[CollectionDisplayNodeView]
)

object CollectionSearchDetailsView {
  val selector: SelectionBuilder[CollectionSearchDetails, CollectionSearchDetailsView] =
    (
      CollectionSearchDetails.attDisplay ~
        CollectionSearchDetails.disableThumbnail ~
        CollectionSearchDetails.standardOpen ~
        CollectionSearchDetails.integrationOpen ~
        CollectionSearchDetails.displayNodes(CollectionDisplayNodeView.selector)
    ).mapN(CollectionSearchDetailsView.apply _)
}
