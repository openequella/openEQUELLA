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

object InternalGroupQueries {

  /** Retrieve a group by its unique ID
    */
  def byId[A](uniqueId: String)(innerSelection: SelectionBuilder[InternalGroup, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[InternalGroupQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "byId",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("uniqueId", uniqueId, "String!"))
    )

  /** Retrieve a group by its name
    */
  def byName[A](name: String)(innerSelection: SelectionBuilder[InternalGroup, A])(implicit
      encoder0: ArgEncoder[String]
  ): SelectionBuilder[InternalGroupQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "byName",
      OptionOf(Obj(innerSelection)),
      arguments = List(Argument("name", name, "String!"))
    )

  /** List all groups at a specific level in the hierarchy determined by the parent ID - or none for
    * the root.
    */
  def list[A](
      parentId: scala.Option[String] = None,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[InternalGroupConnection, A])(implicit
      encoder0: ArgEncoder[scala.Option[String]],
      encoder1: ArgEncoder[scala.Option[Int]]
  ): SelectionBuilder[InternalGroupQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "list",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("parentId", parentId, "String"),
        Argument("first", first, "Int"),
        Argument("last", last, "Int"),
        Argument("before", before, "String"),
        Argument("after", after, "String")
      )
    )

  /** List multiple groups by their unique IDs, invalid IDs will be ignored
    */
  def listByIds[A](
      uniqueIds: List[String] = Nil,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[InternalGroupConnection, A])(implicit
      encoder0: ArgEncoder[List[String]],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[InternalGroupQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "listByIds",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("uniqueIds", uniqueIds, "[String!]!"),
        Argument("first", first, "Int"),
        Argument("last", last, "Int"),
        Argument("before", before, "String"),
        Argument("after", after, "String")
      )
    )

  /** Search for groups anywhere within the hierarchy by name (wildcard search)
    */
  def search[A](
      query: String,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[InternalGroupConnection, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[InternalGroupQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "search",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("query", query, "String!"),
        Argument("first", first, "Int"),
        Argument("last", last, "Int"),
        Argument("before", before, "String"),
        Argument("after", after, "String")
      )
    )

  /** List user ids for all users in the specified group
    */
  def users[A](
      uniqueId: String,
      first: scala.Option[Int] = None,
      last: scala.Option[Int] = None,
      before: scala.Option[String] = None,
      after: scala.Option[String] = None
  )(innerSelection: SelectionBuilder[StringConnection, A])(implicit
      encoder0: ArgEncoder[String],
      encoder1: ArgEncoder[scala.Option[Int]],
      encoder2: ArgEncoder[scala.Option[String]]
  ): SelectionBuilder[InternalGroupQueries, scala.Option[A]] =
    _root_.caliban.client.SelectionBuilder.Field(
      "users",
      OptionOf(Obj(innerSelection)),
      arguments = List(
        Argument("uniqueId", uniqueId, "String!"),
        Argument("first", first, "Int"),
        Argument("last", last, "Int"),
        Argument("before", before, "String"),
        Argument("after", after, "String")
      )
    )
}
