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

object CollectionQueries {

  /** List all collections
    */
  def list[A](
      includeSystem: scala.Option[Boolean] = None
  )(innerSelection: SelectionBuilder[BaseEntityReference, A])(implicit
      encoder0: ArgEncoder[scala.Option[Boolean]]
  ): SelectionBuilder[CollectionQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "list",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("includeSystem", includeSystem, "Boolean"))
  )

  /** Export a collection, returning a base64 encoded zip file
    */
  def `export`(id: Long, withSecurity: Boolean)(implicit
      encoder0: ArgEncoder[Long],
      encoder1: ArgEncoder[Boolean]
  ): SelectionBuilder[CollectionQueries, scala.Option[String]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "export",
      OptionOf(Scalar()),
      arguments =
        List(Argument("id", id, "Long!"), Argument("withSecurity", withSecurity, "Boolean!"))
    )

  /** Get the collection ID for a given UUID
    */
  def idForUuid(value: String)(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[CollectionQueries, scala.Option[Long]] =
    _root_.caliban.client.SelectionBuilder
      .Field("idForUuid", OptionOf(Scalar()), arguments = List(Argument("value", value, "String!")))

  /** Get a collection by ID
    */
  def byId[A](id: Long)(innerSelection: SelectionBuilder[CollectionDefinition, A])(implicit
      encoder0: ArgEncoder[Long]
  ): SelectionBuilder[CollectionQueries, scala.Option[A]] = _root_.caliban.client.SelectionBuilder
    .Field("byId", OptionOf(Obj(innerSelection)), arguments = List(Argument("id", id, "Long!")))

  /** Get a collection by UUID
    */
  def byUuid[A](uuid: String)(
      innerSelection: SelectionBuilder[CollectionDefinition, A]
  )(implicit encoder0: ArgEncoder[String]): SelectionBuilder[CollectionQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "byUuid",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("uuid", uuid, "String!"))
    )

  /** List the wizard categories in use across all collections
    */
  def categories: SelectionBuilder[CollectionQueries, List[String]] =
    _root_.caliban.client.SelectionBuilder.Field("categories", ListOf(Scalar()))

  /** List the collections which use the specified schema
    */
  def listForSchema[A](schemaId: Long)(innerSelection: SelectionBuilder[BaseEntityReference, A])(
      implicit encoder0: ArgEncoder[Long]
  ): SelectionBuilder[CollectionQueries, List[A]] = _root_.caliban.client.SelectionBuilder.Field(
    "listForSchema",
    ListOf(Obj(innerSelection)),
    arguments = List(Argument("schemaId", schemaId, "Long!"))
  )
}
