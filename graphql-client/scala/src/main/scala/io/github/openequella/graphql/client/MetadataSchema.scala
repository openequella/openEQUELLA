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

object MetadataSchema {

  /** Details of the metadata schema, including UUID and owner
    */
  def details[A](
      innerSelection: SelectionBuilder[EntityDetails, A]
  ): SelectionBuilder[MetadataSchema, A] =
    _root_.caliban.client.SelectionBuilder.Field("details", Obj(innerSelection))

  /** Transforms applied when exporting this schema between repositories
    */
  def exportTransforms[A](
      innerSelection: SelectionBuilder[MetadataSchemaTransform, A]
  ): SelectionBuilder[MetadataSchema, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("exportTransforms", ListOf(Obj(innerSelection)))

  /** Transforms applied when importing this schema between repositories
    */
  def importTransforms[A](
      innerSelection: SelectionBuilder[MetadataSchemaTransform, A]
  ): SelectionBuilder[MetadataSchema, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("importTransforms", ListOf(Obj(innerSelection)))

  /** Path to the name attribute in the schema
    */
  def itemNamePath: SelectionBuilder[MetadataSchema, String] =
    _root_.caliban.client.SelectionBuilder.Field("itemNamePath", Scalar())

  /** Path to the description attribute in the schema
    */
  def itemDescriptionPath: SelectionBuilder[MetadataSchema, String] =
    _root_.caliban.client.SelectionBuilder.Field("itemDescriptionPath", Scalar())

  /** XML definition of the schema
    */
  def definition: SelectionBuilder[MetadataSchema, String] =
    _root_.caliban.client.SelectionBuilder.Field("definition", Scalar())

  /** List of citations associated with this schema
    */
  def citations[A](
      innerSelection: SelectionBuilder[Citation, A]
  ): SelectionBuilder[MetadataSchema, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field("citations", ListOf(Obj(innerSelection)))
}
