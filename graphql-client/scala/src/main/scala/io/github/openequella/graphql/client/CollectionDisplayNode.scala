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

package io.github.openequella.graphql.client

import caliban.client.FieldBuilder._
import caliban.client._

object CollectionDisplayNode {

  /** XPath to the metadata node
    */
  def node: SelectionBuilder[CollectionDisplayNode, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("node", OptionOf(Scalar()))

  /** The type of display for this node
    */
  def nodeType: SelectionBuilder[CollectionDisplayNode, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("nodeType", OptionOf(Scalar()))

  /** The display mode for this node
    */
  def mode: SelectionBuilder[CollectionDisplayNode, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("mode", OptionOf(Scalar()))

  /** Splitter character between repeated node values
    */
  def splitter: SelectionBuilder[CollectionDisplayNode, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("splitter", OptionOf(Scalar()))

  /** Display title for this node
    */
  def title[A](
      innerSelection: SelectionBuilder[LanguageBundle, A]
  ): SelectionBuilder[CollectionDisplayNode, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field("title", OptionOf(Obj(innerSelection)))

  /** Maximum display length before truncation
    */
  def truncateLength: SelectionBuilder[CollectionDisplayNode, scala.Option[Int]] =
    _root_.caliban.client.SelectionBuilder.Field("truncateLength", OptionOf(Scalar()))
}
