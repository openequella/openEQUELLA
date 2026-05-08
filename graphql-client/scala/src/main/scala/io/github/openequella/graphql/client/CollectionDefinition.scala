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

object CollectionDefinition {

  /** Common details of the collection definition, including UUID and owner.
    */
  def details[A](
      innerSelection: SelectionBuilder[EntityDetails, A]
  ): SelectionBuilder[CollectionDefinition, A] =
    _root_.caliban.client.SelectionBuilder.Field("details", Obj(innerSelection))

  /** ID of the metadata schema associated with this collection.
    */
  def schemaId: SelectionBuilder[CollectionDefinition, scala.Option[Long]] =
    _root_.caliban.client.SelectionBuilder.Field("schemaId", OptionOf(Scalar()))

  /** Category identifier for the wizard used by this collection.
    */
  def wizardCategory: SelectionBuilder[CollectionDefinition, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("wizardCategory", OptionOf(Scalar()))

  /** ID of the workflow associated with this collection.
    */
  def workflowId: SelectionBuilder[CollectionDefinition, scala.Option[Long]] =
    _root_.caliban.client.SelectionBuilder.Field("workflowId", OptionOf(Scalar()))

  /** The review period in days. None if no review period is configured for this collection.
    */
  def reviewPeriod: SelectionBuilder[CollectionDefinition, scala.Option[Int]] =
    _root_.caliban.client.SelectionBuilder.Field("reviewPeriod", OptionOf(Scalar()))

  /** Name of the XSLT used for SCORM packaging.
    */
  def scormPackagingTransformation: SelectionBuilder[CollectionDefinition, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field("scormPackagingTransformation", OptionOf(Scalar()))

  /** Whether direct contribution by users is denied for this collection.
    */
  def denyDirectContribution: SelectionBuilder[CollectionDefinition, Boolean] =
    _root_.caliban.client.SelectionBuilder.Field("denyDirectContribution", Scalar())

  /** Contribution wizard configuration for this collection.
    */
  def wizard[A](
      innerSelection: SelectionBuilder[CollectionWizard, A]
  ): SelectionBuilder[CollectionDefinition, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field("wizard", OptionOf(Obj(innerSelection)))

  /** Search results display configuration for this collection.
    */
  def searchDetails[A](
      innerSelection: SelectionBuilder[CollectionSearchDetails, A]
  ): SelectionBuilder[CollectionDefinition, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field("searchDetails", OptionOf(Obj(innerSelection)))

  /** Metadata import mapping configuration for this collection.
    */
  def metadataMapping[A](
      innerSelection: SelectionBuilder[CollectionMetadataMapping, A]
  ): SelectionBuilder[CollectionDefinition, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field("metadataMapping", OptionOf(Obj(innerSelection)))

  /** Rules controlling access to items in this collection based on their metadata.
    */
  def itemMetadataRules[A](
      innerSelection: SelectionBuilder[CollectionItemMetadataRule, A]
  ): SelectionBuilder[CollectionDefinition, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("itemMetadataRules", ListOf(Obj(innerSelection)))

  /** Rules that apply dynamic ACL entries to items based on their metadata.
    */
  def dynamicMetadataRules[A](
      innerSelection: SelectionBuilder[CollectionDynamicMetadataRule, A]
  ): SelectionBuilder[CollectionDefinition, List[A]] = _root_.caliban.client.SelectionBuilder
    .Field("dynamicMetadataRules", ListOf(Obj(innerSelection)))

  /** Summary page display template for items in this collection.
    */
  def itemSummaryDisplayTemplate[A](
      innerSelection: SelectionBuilder[CollectionSummaryDisplayTemplate, A]
  ): SelectionBuilder[CollectionDefinition, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder
      .Field("itemSummaryDisplayTemplate", OptionOf(Obj(innerSelection)))
}
