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

object CollectionWizard {

  /** Display name of the wizard
    */
  def name: SelectionBuilder[CollectionWizard, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("name", OptionOf(Scalar()))

  /** Script to run when an item is redrafted
    */
  def redraftScript: SelectionBuilder[CollectionWizard, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("redraftScript", OptionOf(Scalar()))

  /** Script to run when an item is saved
    */
  def saveScript: SelectionBuilder[CollectionWizard, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("saveScript", OptionOf(Scalar()))

  /** Whether contributors can navigate wizard pages non-sequentially
    */
  def allowNonSequentialNavigation: SelectionBuilder[CollectionWizard, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("allowNonSequentialNavigation", Scalar())

  /** Whether to show page titles in the next/previous navigation
    */
  def showPageTitlesNextPrev: SelectionBuilder[CollectionWizard, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("showPageTitlesNextPrev", Scalar())

  /** Additional CSS class applied to the wizard container
    */
  def additionalCssClass: SelectionBuilder[CollectionWizard, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("additionalCssClass", OptionOf(Scalar()))

  /** Internal use only. Opaque serialised XML of the wizard pages. Do not parse this field directly
    * ? use dedicated APIs for wizard page manipulation.
    */
  def pages: SelectionBuilder[CollectionWizard, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("pages", OptionOf(Scalar()))

  /** Internal use only. Opaque serialised XML of the fixed metadata applied by the wizard.
    */
  def fixedMetadata: SelectionBuilder[CollectionWizard, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("fixedMetadata", OptionOf(Scalar()))
}
