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

object CollectionSearchDetails {

  /** Controls how attachments are displayed in search results
    */
  def attDisplay: SelectionBuilder[CollectionSearchDetails, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("attDisplay", OptionOf(Scalar()))

  /** Whether to disable the thumbnail display in search results
    */
  def disableThumbnail: SelectionBuilder[CollectionSearchDetails, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("disableThumbnail", Scalar())

  /** Whether search results open in the standard view by default
    */
  def standardOpen: SelectionBuilder[CollectionSearchDetails, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("standardOpen", Scalar())

  /** Whether search results open in integration contexts by default
    */
  def integrationOpen: SelectionBuilder[CollectionSearchDetails, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("integrationOpen", Scalar())

  /** The list of metadata nodes to display for each item in search results
    */
  def displayNodes[A](
      innerSelection: SelectionBuilder[CollectionDisplayNode, A]
  ): SelectionBuilder[CollectionSearchDetails, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("displayNodes", ListOf(Obj(innerSelection)))
}
