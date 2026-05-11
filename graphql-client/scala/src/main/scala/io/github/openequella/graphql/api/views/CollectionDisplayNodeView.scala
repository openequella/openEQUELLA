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
import io.github.openequella.graphql.client.CollectionDisplayNode

/** View model for a display node configuration in search results or summary pages.
  *
  * @param node
  *   XPath to the metadata node.
  * @param nodeType
  *   The type of display for this node.
  * @param mode
  *   The display mode for this node.
  * @param splitter
  *   Splitter character between repeated node values.
  * @param title
  *   Display title for this node.
  * @param truncateLength
  *   Maximum display length before truncation.
  */
final case class CollectionDisplayNodeView(
    node: Option[String],
    nodeType: Option[String],
    mode: Option[String],
    splitter: Option[String],
    title: Option[LanguageBundleView],
    truncateLength: Option[Int]
)

object CollectionDisplayNodeView {
  val selector: SelectionBuilder[CollectionDisplayNode, CollectionDisplayNodeView] =
    (
      CollectionDisplayNode.node ~
        CollectionDisplayNode.nodeType ~
        CollectionDisplayNode.mode ~
        CollectionDisplayNode.splitter ~
        CollectionDisplayNode.title(LanguageBundleView.selector) ~
        CollectionDisplayNode.truncateLength
    ).mapN(CollectionDisplayNodeView.apply _)
}
