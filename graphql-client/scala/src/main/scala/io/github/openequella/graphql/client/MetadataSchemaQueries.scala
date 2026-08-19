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

object MetadataSchemaQueries {

  /** List all metadata schemas
    */
  def list[A](
      includeSystem: scala.Option[Boolean] = None
  )(innerSelection: SelectionBuilder[BaseEntityReference, A])(implicit
      encoder0: ArgEncoder[scala.Option[Boolean]]
  ): SelectionBuilder[MetadataSchemaQueries, List[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "list",
      ListOf(Obj(innerSelection)),
      arguments = List(Argument("includeSystem", includeSystem, "Boolean"))
    )

  /** Export a metadata schema, returning a base64 encoded zip file
    */
  def `export`(id: Long, withSecurity: Boolean)(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[Boolean]
  ): SelectionBuilder[MetadataSchemaQueries, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "export",
      OptionOf(Scalar()),
      arguments =
        List(Argument("id", id, "Long!"), Argument("withSecurity", withSecurity, "Boolean!"))
    )

  /** Get the metadata schema ID for a given UUID
    */
  def idForUuid(value: String)(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[MetadataSchemaQueries, scala.Option[Long]] =
    _root_.caliban.client.SelectionBuilder
      .Field("idForUuid", OptionOf(Scalar()), arguments = List(Argument("value", value, "String!")))

  /** Get a metadata schema by ID
    */
  def byId[A](id: Long)(
      innerSelection: SelectionBuilder[MetadataSchema, A]
  )(implicit encoder0: ArgEncoder[Long]): SelectionBuilder[MetadataSchemaQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder
      .Field("byId", OptionOf(Obj(innerSelection)), arguments = List(Argument("id", id, "Long!")))

  /** Get the uses of a metadata schema by ID
    */
  def uses[A](id: Long)(innerSelection: SelectionBuilder[BaseEntityReference, A])(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[MetadataSchemaQueries, List[A]] = _root_.caliban.client.SelectionBuilder
    .Field("uses", ListOf(Obj(innerSelection)), arguments = List(Argument("id", id, "Long!")))

  /** Get the types of schema import transformations for a metadata schema by ID
    */
  def importTypes(id: Long)(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[MetadataSchemaQueries, List[String]] = _root_.caliban.client.SelectionBuilder
    .Field("importTypes", ListOf(Scalar()), arguments = List(Argument("id", id, "Long!")))

  /** Check if a metadata schema has an referencing entities
    */
  def hasReferences(id: Long)(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[MetadataSchemaQueries, Boolean] = _root_.caliban.client.SelectionBuilder
    .Field("hasReferences", Scalar(), arguments = List(Argument("id", id, "Long!")))
}
