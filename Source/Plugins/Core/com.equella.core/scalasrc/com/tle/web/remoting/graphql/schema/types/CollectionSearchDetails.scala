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
import com.tle.beans.entity.itemdef.{DisplayNode, SearchDetails}

import scala.jdk.CollectionConverters._

/** GraphQL representation of `com.tle.beans.entity.itemdef.SearchDetails`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.SearchDetails]]
  */
@GQLDescription("Configuration for how a collection's items are displayed in search results.")
final case class CollectionSearchDetails(
    @GQLDescription("Controls how attachments are displayed in search results")
    attDisplay: Option[String],
    @GQLDescription("Whether to disable the thumbnail display in search results")
    disableThumbnail: Boolean,
    @GQLDescription("Whether search results open in the standard view by default")
    standardOpen: Boolean,
    @GQLDescription("Whether search results open in integration contexts by default")
    integrationOpen: Boolean,
    @GQLDescription("The list of metadata nodes to display for each item in search results")
    displayNodes: List[CollectionDisplayNode]
)

object CollectionSearchDetails {
  def apply(searchDetails: SearchDetails): CollectionSearchDetails =
    CollectionSearchDetails(
      attDisplay = Option(searchDetails.getAttDisplay),
      disableThumbnail = searchDetails.isDisableThumbnail,
      standardOpen = searchDetails.isStandardOpen,
      integrationOpen = searchDetails.isIntegrationOpen,
      displayNodes = Option(searchDetails.getDisplayNodes)
        .map(_.asScala.toList.map(CollectionDisplayNode(_)))
        .getOrElse(List.empty)
    )
}

/** GraphQL representation of `com.tle.beans.entity.itemdef.DisplayNode`.
  *
  * @see
  *   [[com.tle.beans.entity.itemdef.DisplayNode]]
  */
@GQLDescription("A node in the metadata to display for items in search results.")
final case class CollectionDisplayNode(
    @GQLDescription("XPath to the metadata node")
    node: Option[String],
    @GQLDescription("The type of display for this node")
    nodeType: Option[String],
    @GQLDescription("The display mode for this node")
    mode: Option[String],
    @GQLDescription("Splitter character between repeated node values")
    splitter: Option[String],
    @GQLDescription("Display title for this node")
    title: Option[LanguageBundle],
    @GQLDescription("Maximum display length before truncation")
    truncateLength: Option[Int]
)

object CollectionDisplayNode {
  def apply(displayNode: DisplayNode): CollectionDisplayNode =
    CollectionDisplayNode(
      node = Option(displayNode.getNode),
      nodeType = Option(displayNode.getType),
      mode = Option(displayNode.getMode),
      splitter = Option(displayNode.getSplitter),
      title = Option(displayNode.getTitle).map(LanguageBundle(_)),
      truncateLength = Option(displayNode.getTruncateLength).map(_.toInt)
    )
}
